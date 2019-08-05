#import <Flutter/Flutter.h>
#import <OPPWAMobile/OPPWAMobile.h>

@interface FlutterPeachpayPlugin : NSObject<FlutterPlugin> {
    OPPPaymentProvider *provider;
    NSString *checkoutID;
}
@end
