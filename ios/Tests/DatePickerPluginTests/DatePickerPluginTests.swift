import XCTest
@testable import DatePickerPlugin

class DatePickerTests: XCTestCase {

    func testDateRoundTrip() {
        let format = "yyyy-MM-dd'T'HH:mm:ss"
        let value = "2021-02-12T10:30:00"

        let date = Parse.dateFromString(date: value, format: format)
        let result = Parse.dateToString(date: date, format: format)

        XCTAssertEqual(value, result)
    }
}
