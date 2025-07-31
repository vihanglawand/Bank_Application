import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

class BalanceTab extends StatefulWidget {
  final String authToken;
  final String accountNumber; // ✅ Add this

  const BalanceTab({
    super.key,
    required this.authToken,
    required this.accountNumber,
  });

  @override
  State<BalanceTab> createState() => _BalanceTabState();
}

class _BalanceTabState extends State<BalanceTab> {
  String? balance;
  String error = '';
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    _fetchBalance();
  }

  Future<void> _fetchBalance() async {
    final url = Uri.parse('http://localhost:8080/api/account/${widget.accountNumber}/balance');

    try {
      final response = await http.get(
        url,
        headers: {
          'Authorization': 'Bearer ${widget.authToken}',
        },
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body); // returns 475.0 directly
        setState(() {
          balance = data.toString(); // ✔ Convert number to string directly
          isLoading = false;
        });
      } else {
        setState(() {
          error = 'Failed to fetch balance: ${response.statusCode}';
          isLoading = false;
        });
      }
    } catch (e) {
      setState(() {
        error = 'Error: $e';
        isLoading = false;
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
        child: isLoading
            ? const Center(child: CircularProgressIndicator())
            : error.isNotEmpty
            ? Center(child: Text(error, style: const TextStyle(color: Colors.red)))
            : Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.account_balance_wallet, size: 64, color: Colors.indigo),
            const SizedBox(height: 16),
            const Text(
              'Current Balance',
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            Text(
              balance != null ? '₹ $balance' : 'No balance data',
              style: const TextStyle(
                fontSize: 36,
                fontWeight: FontWeight.bold,
                color: Colors.indigo,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
