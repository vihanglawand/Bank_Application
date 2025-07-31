import 'package:flutter/material.dart';
import 'package:vihang_bank/trans_screen.dart';
import 'package:vihang_bank/transfer_screen.dart';
import 'package:vihang_bank/withdraw_screen.dart';
import 'bal_screen.dart';
import 'deposite_screen.dart';
import 'main.dart';

class DashboardPage extends StatefulWidget {
  final String authToken;
  final String username;
  final String phoneNumber;
  final String accountNumber;

  const DashboardPage({
    super.key,
    required this.authToken,
    required this.username,
    required this.phoneNumber,
    required this.accountNumber,
  });

  @override
  State<DashboardPage> createState() => _DashboardPageState();
}

class _DashboardPageState extends State<DashboardPage> {
  int _selectedIndex = 0;

  final List<Map<String, dynamic>> features = [
    {
      'title': 'Deposit',
      'icon': Icons.account_balance_wallet,
      'color': Colors.green,
    },
    {
      'title': 'Withdraw',
      'icon': Icons.money_off,
      'color': Colors.red,
    },
    {
      'title': 'Transfer Money',
      'icon': Icons.send,
      'color': Colors.blue,
    },
    {
      'title': 'Transaction History',
      'icon': Icons.history,
      'color': Colors.orange,
    },
    {
      'title': 'Balance',
      'icon': Icons.account_balance,
      'color': Colors.purple,
    },
  ];

  @override
  Widget build(BuildContext context) {
    final selectedFeature = features[_selectedIndex];

    return Scaffold(
      appBar: AppBar(
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('AL Bank Dashboard'),
            Text(
              'User: ${widget.username} | Account: ${widget.accountNumber}',
              style: const TextStyle(fontSize: 14),
            ),
          ],
        ),
        backgroundColor: Theme.of(context).colorScheme.primary,
        foregroundColor: Colors.white,
        centerTitle: false,
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            tooltip: 'Logout',
            onPressed: () {
              Navigator.pushAndRemoveUntil(
                context,
                MaterialPageRoute(
                  builder: (context) => const MyHomePage(title: 'Login'),
                ),
                    (route) => false,
              );
            },
          ),
        ],
      ),
      body: Row(
        children: [
          NavigationRail(
            selectedIndex: _selectedIndex,
            onDestinationSelected: (index) {
              setState(() {
                _selectedIndex = index;
              });
            },
            labelType: NavigationRailLabelType.all,
            destinations: features
                .map(
                  (f) => NavigationRailDestination(
                icon: Icon(f['icon']),
                label: Text(f['title']),
              ),
            )
                .toList(),
          ),
          const VerticalDivider(width: 1),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: _buildFeatureWidget(selectedFeature),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFeatureWidget(Map<String, dynamic> feature) {
    switch (_selectedIndex) {
      case 0:
        return DepositTab(
          authToken: widget.authToken,
          accountNumber: widget.accountNumber,
        );
      case 1:
        return WithdrawTab(
          authToken: widget.authToken,
         accountNumber: widget.accountNumber,
        );
      case 2:
        return TransferTab(
          authToken: widget.authToken,
          //fromAccountNumber: widget.accountNumber,
        );
      case 3:
        return TransactionHistoryTab(
          authToken: widget.authToken,
          accountNumber: widget.accountNumber,

        );
      case 4:
        return BalanceTab(
          authToken: widget.authToken,
          accountNumber: widget.accountNumber,
        );
      default:
        return const Center(child: Text("Unknown Feature"));
    }
  }

  Widget _comingSoonCard(String title, IconData icon, Color color) {
    return Card(
      elevation: 10,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      child: Container(
        padding: const EdgeInsets.all(32),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(24),
          gradient: LinearGradient(
            colors: [color.withOpacity(0.1), color.withOpacity(0.03)],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            Icon(icon, size: 64, color: color),
            const SizedBox(height: 16),
            Text(
              title,
              style: TextStyle(
                fontSize: 28,
                fontWeight: FontWeight.bold,
                color: color,
              ),
            ),
            const SizedBox(height: 24),
            const Text(
              'Feature coming soon!',
              style: TextStyle(fontSize: 18),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}
