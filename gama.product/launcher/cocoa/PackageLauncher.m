#import <Cocoa/Cocoa.h>
#import <Carbon/Carbon.h>
#include <jni.h>

@interface GamaStartupDocuments : NSObject
@property(nonatomic, strong) NSMutableArray<NSString *> *paths;
@property(nonatomic) BOOL debug;
- (void)open:(NSAppleEventDescriptor *)event reply:(NSAppleEventDescriptor *)reply;
@end

static OSErr captureDocuments(const AppleEvent *event, AppleEvent *reply, SRefCon refcon) {
    AEDesc copy;
    OSErr status = AEDuplicateDesc(event, &copy);
    if (status != noErr) return status;
    NSAppleEventDescriptor *descriptor = [[NSAppleEventDescriptor alloc] initWithAEDescNoCopy:&copy];
    GamaStartupDocuments *receiver = (__bridge GamaStartupDocuments *)(void *)refcon;
    [receiver open:descriptor reply:nil];
    return noErr;
}

@implementation GamaStartupDocuments
- (void)open:(NSAppleEventDescriptor *)event reply:(NSAppleEventDescriptor *)reply {
    NSAppleEventDescriptor *items = [event paramDescriptorForKeyword:keyDirectObject];
    if (self.debug) fprintf(stderr, "Startup AppleEvent: %s\n", event.description.UTF8String);
    for (NSInteger i = 1; i <= items.numberOfItems; i++) {
        NSAppleEventDescriptor *item = [[items descriptorAtIndex:i] coerceToDescriptorType:typeFileURL];
        NSString *text = item == nil ? nil : [[NSString alloc] initWithData:item.data encoding:NSUTF8StringEncoding];
        NSURL *url = text == nil ? nil : [NSURL URLWithString:text];
        if (url.isFileURL) [self.paths addObject:url.path];
    }
}
@end

static NSString *string(JNIEnv *env, jstring input) {
    if (input == NULL) return @"";
    const jchar *characters = (*env)->GetStringChars(env, input, NULL);
    if (characters == NULL) return @"";
    NSString *result = [[NSString alloc] initWithCharacters:(const unichar *)characters
                                                   length:(NSUInteger)(*env)->GetStringLength(env, input)];
    (*env)->ReleaseStringChars(env, input, characters);
    return result;
}

JNIEXPORT jobjectArray JNICALL Java_gama_launcher_PackageLauncher_startupDocuments(JNIEnv *env, jclass type, jboolean debug) {
    @autoreleasepool {
        [NSApplication sharedApplication];
        AEEventHandlerUPP previous = NULL;
        SRefCon previousRefcon = 0;
        OSErr previousStatus = AEGetEventHandler(kCoreEventClass, kAEOpenDocuments, &previous, &previousRefcon, false);
        GamaStartupDocuments *receiver = [GamaStartupDocuments new];
        receiver.debug = debug;
        receiver.paths = [NSMutableArray new];
        AEEventHandlerUPP handler = NewAEEventHandlerUPP(captureDocuments);
        OSErr installed = AEInstallEventHandler(kCoreEventClass, kAEOpenDocuments, handler,
                                                (SRefCon)(__bridge void *)receiver, false);
        if (installed != noErr) {
            DisposeAEEventHandlerUPP(handler);
            (*env)->ThrowNew(env, (*env)->FindClass(env, "java/io/IOException"),
                            "Unable to collect startup documents");
            return NULL;
        }
        [NSApp finishLaunching];
        if (debug) fprintf(stderr, "Collecting startup AppleEvents on main thread: %s\n", NSThread.isMainThread ? "yes" : "no");
        NSDate *deadline = [NSDate dateWithTimeIntervalSinceNow:0.5];
        while (deadline.timeIntervalSinceNow > 0) {
            NSEvent *event = [NSApp nextEventMatchingMask:NSEventMaskAny
                                              untilDate:[NSDate dateWithTimeIntervalSinceNow:0.01]
                                                 inMode:NSDefaultRunLoopMode dequeue:YES];
            if (event != nil) [NSApp sendEvent:event];
        }
        OSErr removed = AERemoveEventHandler(kCoreEventClass, kAEOpenDocuments, handler, false);
        OSErr restored = noErr;
        if (previousStatus == noErr)
            restored = AEInstallEventHandler(kCoreEventClass, kAEOpenDocuments, previous, previousRefcon, false);
        DisposeAEEventHandlerUPP(handler);
        if (removed != noErr || restored != noErr) {
            (*env)->ThrowNew(env, (*env)->FindClass(env, "java/io/IOException"),
                            "Unable to restore the native document handler");
            return NULL;
        }
        jobjectArray result = (*env)->NewObjectArray(env, (jsize)receiver.paths.count,
                                                     (*env)->FindClass(env, "java/lang/String"), NULL);
        for (NSUInteger i = 0; i < receiver.paths.count; i++) {
            NSString *path = receiver.paths[i];
            unichar *characters = malloc(path.length * sizeof(unichar));
            [path getCharacters:characters range:NSMakeRange(0, path.length)];
            jstring value = (*env)->NewString(env, characters, (jsize)path.length);
            free(characters);
            (*env)->SetObjectArrayElement(env, result, (jsize)i, value);
            (*env)->DeleteLocalRef(env, value);
        }
        return result;
    }
}

JNIEXPORT void JNICALL Java_gama_launcher_PackageLauncher_composeSplash(
        JNIEnv *env, jclass type, jstring imagePath, jstring titleValue, jstring descriptionValue, jstring output) {
    @autoreleasepool {
        NSString *title = string(env, titleValue);
        if (title.length == 0) title = @"Simulation";
        NSString *image = string(env, imagePath);
        NSImage *illustration = image.length == 0 ? nil : [[NSImage alloc] initWithContentsOfFile:image];
        if (image.length > 0 && illustration == nil) {
            (*env)->ThrowNew(env, (*env)->FindClass(env, "java/io/IOException"),
                            "Unable to read the package presentation image");
            return;
        }
        NSBitmapImageRep *bitmap = [[NSBitmapImageRep alloc] initWithBitmapDataPlanes:NULL
            pixelsWide:680 pixelsHigh:520 bitsPerSample:8 samplesPerPixel:4 hasAlpha:YES
            isPlanar:NO colorSpaceName:NSDeviceRGBColorSpace bytesPerRow:0 bitsPerPixel:0];
        NSGraphicsContext *context = [NSGraphicsContext graphicsContextWithBitmapImageRep:bitmap];
        [NSGraphicsContext saveGraphicsState];
        NSGraphicsContext.currentContext = context;
        [[NSColor colorWithWhite:0.98 alpha:1] setFill];
        NSRectFill(NSMakeRect(0, 0, 680, 520));
        if (illustration != nil && illustration.size.width > 0 && illustration.size.height > 0) {
            double scale = MIN(1.0, MIN(560.0 / illustration.size.width, 240.0 / illustration.size.height));
            NSSize size = NSMakeSize(illustration.size.width * scale, illustration.size.height * scale);
            [illustration drawInRect:NSMakeRect((680 - size.width) / 2, 110 + (240 - size.height) / 2,
                                               size.width, size.height)];
        }
        NSMutableParagraphStyle *paragraph = [NSMutableParagraphStyle new];
        paragraph.alignment = NSTextAlignmentCenter;
        paragraph.lineBreakMode = NSLineBreakByTruncatingTail;
        [title drawInRect:NSMakeRect(30, 450, 620, 35) withAttributes:@{
            NSFontAttributeName: [NSFont boldSystemFontOfSize:24],
            NSForegroundColorAttributeName: NSColor.darkGrayColor,
            NSParagraphStyleAttributeName: paragraph }];
        [string(env, descriptionValue) drawInRect:NSMakeRect(60, 365, 560, 70) withAttributes:@{
            NSFontAttributeName: [NSFont systemFontOfSize:14],
            NSForegroundColorAttributeName: NSColor.darkGrayColor,
            NSParagraphStyleAttributeName: paragraph }];
        [@"Opening simulation..." drawInRect:NSMakeRect(30, 55, 620, 22) withAttributes:@{
            NSFontAttributeName: [NSFont systemFontOfSize:13],
            NSForegroundColorAttributeName: NSColor.grayColor,
            NSParagraphStyleAttributeName: paragraph }];
        [@"Powered by GAMA" drawInRect:NSMakeRect(30, 25, 620, 22) withAttributes:@{
            NSFontAttributeName: [NSFont systemFontOfSize:13],
            NSForegroundColorAttributeName: NSColor.grayColor,
            NSParagraphStyleAttributeName: paragraph }];
        [NSGraphicsContext restoreGraphicsState];
        NSData *png = [bitmap representationUsingType:NSBitmapImageFileTypePNG properties:@{}];
        NSError *error = nil;
        if (png == nil || ![png writeToFile:string(env, output) options:NSDataWritingAtomic error:&error])
            (*env)->ThrowNew(env, (*env)->FindClass(env, "java/io/IOException"),
                            "Unable to generate the simulation splash image");
    }
}

JNIEXPORT void JNICALL Java_gama_launcher_PackageLauncher_showError(JNIEnv *env, jclass type, jstring message) {
    @autoreleasepool {
        NSAlert *alert = [NSAlert new];
        alert.messageText = @"GAMA";
        alert.informativeText = string(env, message);
        alert.alertStyle = NSAlertStyleCritical;
        [alert runModal];
    }
}
