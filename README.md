
For help getting started with Flutter, view our online
[documentation](https://flutter.io/).


[![Pub](https://img.shields.io/badge/Pub-0.0.1-orange.svg?style=flat-square)](https://pub.dartlang.org/packages/flutter_peachpay_plugin)


# flutter_peachpay_plugin

A Flutter plugin for peach payment gateway for both Android and iOS.

## Usage


```dart
  Future openPaymentGateway() async {
     String tempResult=await PaymentGatewayPlugin.checkoutActitvity("49.99");
     setState(()
     {
       result=tempResult;
     });
   }


```