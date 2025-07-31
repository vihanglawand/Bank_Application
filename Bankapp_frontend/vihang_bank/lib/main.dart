// ✅ Login with Forgot Password
import 'dart:convert';
import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:vihang_bank/sign_up_screen.dart';
import 'dashboard_screen.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'AL Bank',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF0A1D56),
          primary: const Color(0xFF0A1D56),
          secondary: const Color(0xFFFDBF50),
        ),
        scaffoldBackgroundColor: const Color(0xFFE0E7FF),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'Login'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  final String title;

  const MyHomePage({super.key, required this.title});

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  final TextEditingController _usernameController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();

  String _message = '';

  Future<void> _handleLogin() async {
    final String username = _usernameController.text.trim();
    final String password = _passwordController.text;

    final url = Uri.parse('http://localhost:8080/api/auth/login');

    try {
      final response = await http.post(
        url,
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'username': username,
          'password': password,
        }),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        final token = data['token'];
        final accountNumber = data['account_number'].toString();
        final phoneNumber = data['phone_number'];
        final username = data['username'];

        Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder: (context) => DashboardPage(
              authToken: token,
              accountNumber: accountNumber,
              username: username,
              phoneNumber: phoneNumber,
            ),
          ),
        );
      } else {
        setState(() {
          _message = 'Login failed: ${response.statusCode}';
        });
      }
    } catch (error) {
      setState(() {
        _message = 'Error: $error';
      });
    }
  }

  void _showForgotPasswordDialog() {
    final usernameCtrl = TextEditingController();
    final phoneCtrl = TextEditingController();
    final otpCtrl = TextEditingController(text: "123456");
    final newPasswordCtrl = TextEditingController();

    String msg = '';
    bool isLoading = false;

    showDialog(
      context: context,
      builder: (ctx) {
        return StatefulBuilder(builder: (context, setState) {
          Future<void> _submitReset() async {
            if (usernameCtrl.text.isEmpty || phoneCtrl.text.isEmpty || newPasswordCtrl.text.isEmpty) {
              setState(() => msg = "All fields are required.");
              return;
            }

            setState(() => isLoading = true);

            try {
              final res = await http.post(
                Uri.parse('http://localhost:8080/api/auth/forgot-password'),
                headers: {'Content-Type': 'application/json'},
                body: jsonEncode({
                  "username": usernameCtrl.text.trim(),
                  "phonenumber": phoneCtrl.text.trim(),
                  "otp": otpCtrl.text.trim(),
                  "newPassword": newPasswordCtrl.text.trim(),
                }),
              );

              final data = jsonDecode(res.body);
              if (res.statusCode == 200) {
                setState(() => msg = data['message'] ?? 'Password reset successful');
                Future.delayed(const Duration(seconds: 2), () {
                  Navigator.pop(context);
                });
              } else {
                setState(() => msg = data['error'] ?? 'Failed to reset password');
              }
            } catch (e) {
              setState(() => msg = 'Error: $e');
            } finally {
              setState(() => isLoading = false);
            }
          }

          return AlertDialog(
            title: const Text('Forgot Password'),
            content: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: usernameCtrl,
                  decoration: const InputDecoration(labelText: 'Username'),
                ),
                TextField(
                  controller: phoneCtrl,
                  decoration: const InputDecoration(labelText: 'Phone Number'),
                ),
                TextField(
                  controller: otpCtrl,
                  decoration: const InputDecoration(labelText: 'OTP'),
                ),
                TextField(
                  controller: newPasswordCtrl,
                  obscureText: true,
                  decoration: const InputDecoration(labelText: 'New Password'),
                ),
                if (msg.isNotEmpty)
                  Padding(
                    padding: const EdgeInsets.only(top: 8),
                    child: Text(
                      msg,
                      style: TextStyle(color: msg.contains("success") ? Colors.green : Colors.red),
                    ),
                  ),
              ],
            ),
            actions: [
              TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
              ElevatedButton(
                onPressed: isLoading ? null : _submitReset,
                child: isLoading
                    ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2))
                    : const Text('Reset Password'),
              ),
            ],
          );
        });
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final width = MediaQuery.of(context).size.width;

    return Scaffold(
      body: Center(
        child: SingleChildScrollView(
          child: Column(
            children: [
              Text(
                'Vihang Bank',
                style: TextStyle(
                  fontSize: 36,
                  fontWeight: FontWeight.bold,
                  color: Theme.of(context).colorScheme.primary,
                ),
              ),
              const SizedBox(height: 16),
              Icon(Icons.account_balance, size: 64, color: Theme.of(context).colorScheme.primary),
              const SizedBox(height: 32),
              GlassmorphicCard(
                width: width > 600 ? 400 : width * 0.9,
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      Text('Secure Login',
                          style: TextStyle(
                              fontSize: 22, fontWeight: FontWeight.w600, color: Theme.of(context).colorScheme.primary),
                          textAlign: TextAlign.center),
                      const SizedBox(height: 24),
                      TextField(
                        controller: _usernameController,
                        decoration: const InputDecoration(labelText: 'Username'),
                      ),
                      const SizedBox(height: 16),
                      TextField(
                        controller: _passwordController,
                        obscureText: true,
                        decoration: const InputDecoration(labelText: 'Password'),
                      ),
                      const SizedBox(height: 24),
                      ElevatedButton(onPressed: _handleLogin, child: const Text('Login')),
                      const SizedBox(height: 8),
                      TextButton(onPressed: _showForgotPasswordDialog, child: const Text("Forgot Password?")),
                      const SizedBox(height: 8),
                      Text(
                        _message,
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          color: _message.contains("successful") ? Colors.green : Colors.red,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              TextButton(
                onPressed: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (_) => const SignupPage()),
                  );
                },
                child: const Text("Don't have an account? Sign Up"),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class GlassmorphicCard extends StatelessWidget {
  final double width;
  final Widget child;

  const GlassmorphicCard({
    super.key,
    required this.width,
    required this.child,
  });

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(24),
      child: Container(
        width: width,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(24),
          gradient: LinearGradient(
            colors: [Colors.white.withOpacity(0.2), Colors.white.withOpacity(0.05)],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
          border: Border.all(color: Colors.white.withOpacity(0.2)),
        ),
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: 12, sigmaY: 12),
          child: child,
        ),
      ),
    );
  }
}
