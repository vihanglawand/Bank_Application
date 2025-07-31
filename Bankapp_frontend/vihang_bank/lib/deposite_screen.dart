import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

class DepositTab extends StatefulWidget {
  final String authToken;
  final String accountNumber;

  const DepositTab({
    super.key,
    required this.authToken,
    required this.accountNumber,
  });

  @override
  State<DepositTab> createState() => _DepositTabState();
}

class _DepositTabState extends State<DepositTab> {
  final TextEditingController _amountController = TextEditingController();
  String _statusMessage = '';
  bool _isLoading = false;

  Future<void> _makeDeposit() async {
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
        'http://localhost:8080/api/account/${widget.accountNumber}/deposit?amount=$amount');

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
          _statusMessage = 'Deposit successful!';
        });
      } else {
        setState(() {
          _statusMessage =
          'Failed to deposit: ${response.statusCode} ${response.body}';
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
            Icon(Icons.account_balance_wallet,
                size: 64, color: Colors.green[400]),
            const SizedBox(height: 16),
            const Text(
              'Deposit Money',
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
              onPressed: _isLoading ? null : _makeDeposit,
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.green[600],
                foregroundColor: Colors.white,
                padding:
                const EdgeInsets.symmetric(horizontal: 32, vertical: 16),
              ),
              child: _isLoading
                  ? const CircularProgressIndicator(
                  color: Colors.white, strokeWidth: 2)
                  : const Text('Deposit'),
            ),
            const SizedBox(height: 16),
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
