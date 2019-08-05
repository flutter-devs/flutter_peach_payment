import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_peachpay_plugin/flutter_peachpay_plugin.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  String result="";
  @override
  void initState() {
    super.initState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Center(
                child: FloatingActionButton(onPressed: ()  {
                 openPaymentGateway();
                },
                  child: Text("Pay"),)
            ),
            Padding(
              padding: const EdgeInsets.all(20.00),
              child: Text(result),
            )
          ],
        ),
      ),
    );
  }

  Future openPaymentGateway() async {
    String tempResult=await PaymentGatewayPlugin.checkoutActitvity("49.99");
    setState(()
    {
      result=tempResult;
    });
  }
}
