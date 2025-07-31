import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

class TransferTab extends StatefulWidget {
  final String authToken;

  const TransferTab({
    super.key,
    required this.authToken,
  });

  @override
  State<TransferTab> createState() => _TransferTabState();
}

class _TransferTabState extends State<TransferTab> {
  final TextEditingController _toAccountController = TextEditingController();
  final TextEditingController _amountController = TextEditingController();

  String _statusMessage = '';
  bool _isLoading = false;

  Future<void> _makeTransfer() async {
    final toAccount = _toAccountController.text.trim();
    final amount = _amountController.text.trim();

    if (toAccount.isEmpty || amount.isEmpty ||
        double.tryParse(amount) == null) {
      setState(() {
        _statusMessage = 'Please enter a valid account and amount';
      });
      return;
    }

    setState(() {
      _isLoading = true;
      _statusMessage = '';
    });

    final url = Uri.parse('http://localhost:8080/api/account/transfer');

    try {
      final response = await http.post(
        url,
        headers: {
          'Authorization': 'Bearer ${widget.authToken}',
          'Content-Type': 'application/json',
        },
        body: jsonEncode({
          'toAccountId': int.parse(toAccount),
          'amount': double.parse(amount),
        }),
      );

      if (response.statusCode == 200) {
        setState(() {
          _statusMessage = 'Transfer successful!';
        });
      } else {
        String errorMessage = 'Transfer failed';
        try {
          final json = jsonDecode(response.body);
          errorMessage = json['error'] ?? response.body;
        } catch (_) {
          errorMessage = response.body;
        }

        setState(() {
          _statusMessage = '$errorMessage (Code: ${response.statusCode})';
        });
      }
    } catch (e) {
      setState(() {
        _statusMessage = 'Error occurred: $e';
      });
    } finally {
      setState(() {
        _isLoading = false;
        _amountController.clear();
        _toAccountController.clear();
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 12,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.send, size: 64, color: Colors.blue[400]),
            const SizedBox(height: 16),
            const Text(
              'Transfer Money',
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 24),
            TextField(
              controller: _toAccountController,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(
                labelText: 'To Account Number',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _amountController,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(
                labelText: 'Amount',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: _isLoading ? null : _makeTransfer,
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.blue[600],
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(
                    horizontal: 32, vertical: 16),
              ),
              child: _isLoading
                  ? const SizedBox(
                width: 20,
                height: 20,
                child: CircularProgressIndicator(
                  color: Colors.white,
                  strokeWidth: 2,
                ),
              )
                  : const Text('Transfer'),
            ),
            const SizedBox(height: 16),
            if (_statusMessage.isNotEmpty)
              Text(
                _statusMessage,
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w500,
                  color: _statusMessage.contains('successful')
                      ? Colors.green
                      : Colors.red,
                ),
              ),
          ],
        ),
      ),
    );
  }
}