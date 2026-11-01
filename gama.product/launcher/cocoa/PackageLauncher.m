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

static NSString *normalizedText(NSString *text) {
    NSArray<NSString *> *words = [text componentsSeparatedByCharactersInSet:NSCharacterSet.whitespaceAndNewlineCharacterSet];
    return [[words filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"length > 0"]] componentsJoinedByString:@" "];
}

static NSDictionary *textAttributes(NSFont *font, NSColor *color) {
    NSMutableParagraphStyle *paragraph = [NSMutableParagraphStyle new];
    paragraph.lineBreakMode = NSLineBreakByWordWrapping;
    return @{ NSFontAttributeName: font, NSForegroundColorAttributeName: color,
              NSParagraphStyleAttributeName: paragraph };
}

static NSRect topRect(CGFloat x, CGFloat y, CGFloat width, CGFloat height) {
    return NSMakeRect(x, 560 - y - height, width, height);
}

JNIEXPORT void JNICALL Java_gama_launcher_PackageLauncher_composeSplash(
        JNIEnv *env, jclass type, jstring imagePath, jstring titleValue, jstring descriptionValue,
        jbyteArray logoBytes, jstring output) {
    @autoreleasepool {
        NSString *title = normalizedText(string(env, titleValue));
        if (title.length == 0) title = @"Simulation";
        NSString *image = string(env, imagePath);
        NSImage *illustration = image.length == 0 ? nil : [[NSImage alloc] initWithContentsOfFile:image];
        if (image.length > 0 && (illustration == nil || illustration.size.width <= 0 || illustration.size.height <= 0)) {
            (*env)->ThrowNew(env, (*env)->FindClass(env, "java/io/IOException"),
                            "Unable to read the package presentation image");
            return;
        }
        jbyte *bytes = (*env)->GetByteArrayElements(env, logoBytes, NULL);
        if (bytes == NULL) return;
        NSData *logoData = [NSData dataWithBytes:bytes length:(NSUInteger)(*env)->GetArrayLength(env, logoBytes)];
        (*env)->ReleaseByteArrayElements(env, logoBytes, bytes, JNI_ABORT);
        NSImage *logo = [[NSImage alloc] initWithData:logoData];
        if (logo == nil || logo.size.width <= 0 || logo.size.height <= 0) {
            (*env)->ThrowNew(env, (*env)->FindClass(env, "java/io/IOException"),
                            "Unable to read the GAMA splash logo");
            return;
        }
        // Preserve the launcher's 900 x 560 point window while supplying Retina-resolution pixels.
        const CGFloat backingScale = 2;
        NSBitmapImageRep *bitmap = [[NSBitmapImageRep alloc] initWithBitmapDataPlanes:NULL
            pixelsWide:900 * backingScale pixelsHigh:560 * backingScale bitsPerSample:8 samplesPerPixel:4 hasAlpha:YES
            isPlanar:NO colorSpaceName:NSDeviceRGBColorSpace bytesPerRow:0 bitsPerPixel:0];
        bitmap.size = NSMakeSize(900, 560);
        NSGraphicsContext *context = [NSGraphicsContext graphicsContextWithBitmapImageRep:bitmap];
        [NSGraphicsContext saveGraphicsState];
        NSGraphicsContext.currentContext = context;
        context.imageInterpolation = NSImageInterpolationHigh;
        [[NSColor colorWithSRGBRed:247.0/255 green:246.0/255 blue:242.0/255 alpha:1] setFill];
        NSRectFill(NSMakeRect(0, 0, 900, 560));
        [[NSColor colorWithSRGBRed:227.0/255 green:237.0/255 blue:235.0/255 alpha:1] setFill];
        NSRectFill(NSMakeRect(330, 0, 570, 560));
        if (illustration == nil) {
            [logo drawInRect:topRect(495, 160, 240, 240)];
        } else {
            double scale = MAX(570.0 / illustration.size.width, 560.0 / illustration.size.height);
            NSSize crop = NSMakeSize(570 / scale, 560 / scale);
            [illustration drawInRect:NSMakeRect(330, 0, 570, 560)
                fromRect:NSMakeRect((illustration.size.width - crop.width) / 2,
                                    (illustration.size.height - crop.height) / 2, crop.width, crop.height)
                operation:NSCompositingOperationSourceOver fraction:1 respectFlipped:NO hints:nil];
        }
        NSColor *ink = [NSColor colorWithSRGBRed:14.0/255 green:43.0/255 blue:53.0/255 alpha:1];
        NSColor *muted = [NSColor colorWithSRGBRed:81.0/255 green:105.0/255 blue:112.0/255 alpha:1];
        NSStringDrawingOptions options = NSStringDrawingUsesLineFragmentOrigin | NSStringDrawingUsesFontLeading;
        CGFloat fontSize = 32;
        NSDictionary *attributes = textAttributes([NSFont boldSystemFontOfSize:fontSize], ink);
        CGFloat titleHeight = [title boundingRectWithSize:NSMakeSize(274, CGFLOAT_MAX)
            options:options attributes:attributes].size.height;
        while (fontSize > 18 && titleHeight > 200) {
            attributes = textAttributes([NSFont boldSystemFontOfSize:--fontSize], ink);
            titleHeight = [title boundingRectWithSize:NSMakeSize(274, CGFLOAT_MAX)
                options:options attributes:attributes].size.height;
        }
        titleHeight = MIN(200, ceil(titleHeight));
        [title drawWithRect:topRect(28, 42, 274, titleHeight)
            options:options | NSStringDrawingTruncatesLastVisibleLine attributes:attributes];
        CGFloat descriptionTop = 42 + titleHeight + 20;
        [normalizedText(string(env, descriptionValue)) drawWithRect:topRect(28, descriptionTop, 274, MIN(120, 364 - descriptionTop))
            options:options | NSStringDrawingTruncatesLastVisibleLine
            attributes:textAttributes([NSFont systemFontOfSize:14], muted)];
        [@"Opening simulation..." drawInRect:topRect(28, 392, 274, 18)
            withAttributes:textAttributes([NSFont systemFontOfSize:12], muted)];
        [logo drawInRect:topRect(28, 430, 96, 96)];
        [@"powered by" drawInRect:topRect(136, 451, 166, 22)
            withAttributes:textAttributes([NSFont systemFontOfSize:15], muted)];
        [@"GAMA" drawInRect:topRect(136, 470, 166, 36)
            withAttributes:textAttributes([NSFont boldSystemFontOfSize:26], ink)];
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
