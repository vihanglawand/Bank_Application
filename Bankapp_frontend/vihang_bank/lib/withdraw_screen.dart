import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

class WithdrawTab extends StatefulWidget {
  final String authToken;
  final String accountNumber;

  const WithdrawTab({
    super.key,
    required this.authToken,
    required this.accountNumber,
  });

  @override
  State<WithdrawTab> createState() => _WithdrawTabState();
}

class _WithdrawTabState extends State<WithdrawTab> {
  final TextEditingController _amountController = TextEditingController();
  String _statusMessage = '';
  bool _isLoading = false;

  Future<void> _makeWithdraw() async {
    final amount = _amountController.text.trim();

    if (amount.isEmpty || double.tryParse(amount) == null) {
      setState(() {
        _statusMessage = 'Please enter a valid amount';
      });
      return;
    }

    setState(() {
      _isLoading = true;
      _statusMessage = '';
    });

    final url = Uri.parse(
        'http://localhost:8080/api/account/${widget.accountNumber}/withdraw?amount=$amount');

    try {
      final response = await http.post(
        url,
        headers: {
          'Authorization': 'Bearer ${widget.authToken}',
          'Content-Type': 'application/json',
        },
      );

      if (response.statusCode == 200) {
        setState(() {
          _statusMessage = 'Withdrawal successful!';
        });
      } else {
        String errorMsg = 'Failed to withdraw';
        try {
          final json = jsonDecode(response.body);
          errorMsg = json['error'] ?? response.body;
        } catch (_) {
          errorMsg = response.body;
        }

        setState(() {
          _statusMessage = '$errorMsg (Code: ${response.statusCode})';
        });
      }
    } catch (e) {
      setState(() {
        _statusMessage = 'Error: $e';
      });
    } finally {
      setState(() {
        _isLoading = false;
        _amountController.clear();
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
            Icon(Icons.money_off, size: 64, color: Colors.red[400]),
            const SizedBox(height: 16),
            const Text(
              'Withdraw Money',
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 24),
            TextField(
              controller: _amountController,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(
                labelText: 'Enter amount',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: _isLoading ? null : _makeWithdraw,
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.red[600],
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 16),
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
                  : const Text('Withdraw'),
            ),
            const SizedBox(height: 16),
            if (_statusMessage.isNotEmpty)
              Text(
                _statusMessage,
                textAlign: TextAlign.center,
                style: TextStyle(
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
