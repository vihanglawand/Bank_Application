import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

class TransactionHistoryTab extends StatefulWidget {
  final String authToken;
  final String accountNumber;

  const TransactionHistoryTab({
    super.key,
    required this.authToken,
    required this.accountNumber,
  });

  @override
  State<TransactionHistoryTab> createState() => _TransactionHistoryTabState();
}

class _TransactionHistoryTabState extends State<TransactionHistoryTab> {
  List<dynamic> transactions = [];
  bool isLoading = true;
  String error = '';

  @override
  void initState() {
    super.initState();
    _fetchTransactions();
  }

  Future<void> _fetchTransactions() async {
    final url = Uri.parse(
        "http://localhost:8080/api/account/${widget.accountNumber}/transactions",
    );

    try {
      final response = await http.get(
        url,
        headers: {
          'Authorization': 'Bearer ${widget.authToken}',
        },
      );

      if (response.statusCode == 200) {
        setState(() {
          transactions = jsonDecode(response.body);
          isLoading = false;
        });
      } else {
        setState(() {
          error = 'Failed to fetch: ${response.statusCode}';
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
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Transaction History',
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            Expanded(
              child: ListView.separated(
                itemCount: transactions.length,
                separatorBuilder: (_, __) => const Divider(),
                itemBuilder: (context, index) {
                  final tx = transactions[index];
                  return ListTile(
                    leading: Icon(
                      tx['type'] == 'DEPOSIT'
                          ? Icons.arrow_downward
                          : Icons.arrow_upward,
                      color: tx['type'] == 'DEPOSIT' ? Colors.green : Colors.red,
                    ),
                    title: Text(
                      '${tx['type']} ₹${tx['amount']}',
                      style: const TextStyle(fontWeight: FontWeight.w600),
                    ),
                    subtitle: Text(
                      tx['timestamp'] ?? '',
                      style: const TextStyle(color: Colors.grey),
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
