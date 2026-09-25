import XCTest
import UIKit
import WebKit
import Capacitor
@testable import DatePickerPlugin

class DatePickerTests: XCTestCase {
    // The picker is added through the main actor; allow for a loaded machine.
    private let timeout: TimeInterval = 20

    func testDateRoundTrip() {
        let format = "yyyy-MM-dd'T'HH:mm:ss"
        let value = "2021-02-12T10:30:00"

        let date = Parse.dateFromString(date: value, format: format)
        let result = Parse.dateToString(date: date, format: format)

        XCTAssertEqual(value, result)
    }

    @MainActor
    func testPresentRejectsWhenThereIsNoViewController() async {
        let plugin = DatePickerPlugin()
        plugin.load()
        do {
            _ = try await plugin.present(makeCall([:]))
            XCTFail("present must throw")
        } catch {
            XCTAssertEqual((error as? CAPPluginError)?.message, "Unable to access viewController!")
            XCTAssertNil((error as? CAPPluginError)?.code)
        }
    }

    @MainActor
    func testDoneAnswersThePickedDate() async throws {
        let harness = Harness()
        let presented = Task { try await harness.plugin.present(self.makeCall(["mode": "date", "format": "yyyy-MM-dd", "date": "2021-02-12"])) }
        try await waitForPicker(over: harness.root)

        harness.plugin.done(sender: UIButton())

        let result = try await presented.value
        XCTAssertEqual(result["value"] as? String, "2021-02-12")
    }

    @MainActor
    func testCancelAnswersWithoutAValueAndASecondPickerIsRejected() async throws {
        let harness = Harness()
        let presented = Task { try await harness.plugin.present(self.makeCall(["mode": "date"])) }
        try await waitForPicker(over: harness.root)

        // The call that found a picker on screen used to stay pending.
        do {
            _ = try await harness.plugin.present(makeCall(["mode": "date"]))
            XCTFail("a second present must throw")
        } catch {
            XCTAssertEqual((error as? CAPPluginError)?.message, "A date picker is already presented")
        }

        harness.plugin.cancel(sender: UIButton())
        // A second tap while the picker fades out neither crashes nor answers again.
        harness.plugin.cancel(sender: UIButton())

        let result = try await presented.value
        XCTAssertTrue(result.isEmpty)
    }

    func testOnceContinuationResumesOnlyWithTheFirstValue() async {
        let value = await withCheckedContinuation { (continuation: CheckedContinuation<Int, Never>) in
            let once = OnceContinuation(continuation, fallback: 0)
            XCTAssertTrue(once.resume(returning: 1))
            XCTAssertFalse(once.resume(returning: 2))
        }
        XCTAssertEqual(value, 1)
    }

    func testOnceContinuationReleasedWithoutAnswerResumesWithTheFallback() async {
        let value = await withCheckedContinuation { (continuation: CheckedContinuation<Int, Never>) in
            _ = OnceContinuation(continuation, fallback: 7)
        }
        XCTAssertEqual(value, 7)
    }

    /// Waits until the plugin has added its picker to `root`'s view.
    @MainActor
    private func waitForPicker(over root: UIViewController) async throws {
        let deadline = Date().addingTimeInterval(timeout)
        while root.view.subviews.isEmpty {
            guard Date() < deadline else {
                XCTFail("the picker was not shown")
                return
            }
            try await Task.sleep(nanoseconds: 10_000_000)
        }
    }

    private func makeCall(_ options: JSObject) -> CAPPluginCall {
        return CAPPluginCall(callbackId: "test", methodName: "present", options: options, success: { _, _ in
            XCTFail("present answers by returning or throwing")
        }, error: { _ in
            XCTFail("present answers by returning or throwing")
        })
    }
}

/// A loaded plugin whose bridge shows `root`. The plugin's bridge is weak: the harness keeps it.
private struct Harness {
    let plugin = DatePickerPlugin()
    let root = UIViewController()
    let bridge = FakeBridge()

    init() {
        // Loaded before it has a bridge: the fake bridge has no configuration to read.
        plugin.load()
        root.view.frame = CGRect(x: 0, y: 0, width: 390, height: 844)
        bridge.viewController = root
        plugin.bridge = bridge
    }
}

/// A bridge with just enough behaviour for the plugin to find its view controller. Members it never uses trap.
private final class FakeBridge: CAPBridgeProtocol {
    var viewController: UIViewController?
    var webView: WKWebView?
    var isSimEnvironment = true
    var isDevEnvironment = true
    var userInterfaceStyle = UIUserInterfaceStyle.unspecified
    var autoRegisterPlugins = false
    var statusBarVisible = true
    var statusBarStyle = UIStatusBarStyle.default
    var statusBarAnimation = UIStatusBarAnimation.fade
    var config: InstanceConfiguration { fatalError("unused") }
    var notificationRouter: NotificationRouter { fatalError("unused") }

    func plugin(withName: String) -> CAPPlugin? { nil }
    func saveCall(_ call: CAPPluginCall) {}
    func savedCall(withID: String) -> CAPPluginCall? { nil }
    func releaseCall(_ call: CAPPluginCall) {}
    func releaseCall(withID: String) {}
    // swiftlint:disable identifier_name
    func evalWithPlugin(_ plugin: CAPPlugin, js: String) {}
    func eval(js: String) {}
    // swiftlint:enable identifier_name
    func triggerJSEvent(eventName: String, target: String) {}
    func triggerJSEvent(eventName: String, target: String, data: String) {}
    func triggerWindowJSEvent(eventName: String) {}
    func triggerWindowJSEvent(eventName: String, data: String) {}
    func triggerDocumentJSEvent(eventName: String) {}
    func triggerDocumentJSEvent(eventName: String, data: String) {}
    func localURL(fromWebURL webURL: URL?) -> URL? { webURL }
    func portablePath(fromLocalURL localURL: URL?) -> URL? { localURL }
    func setServerBasePath(_ path: String) {}
    func registerPluginType(_ pluginType: CAPPlugin.Type) {}
    func registerPluginInstance(_ pluginInstance: CAPPlugin) {}
    func showAlertWith(title: String, message: String, buttonTitle: String) {}
}
