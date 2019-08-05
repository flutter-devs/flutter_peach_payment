import 'dart:async';

import 'package:flutter/services.dart';

class PaymentGatewayPlugin {
  static const MethodChannel _channel =
  const MethodChannel('flutter_peachpay_plugin');

  static Future<String>  checkoutActitvity(String amt) async {
    final String version = await _channel.invokeMethod('checkoutActivity',{"amt":amt});
    return version;
  }
}
