package com.example.model

data class TestScenario(
    val id: String,
    val title: String,
    val contextCategory: String,
    val contextDescription: String,
    val spokenOrVisibleText: String,
    val screenElements: List<ScreenElement>,
    val primaryExpectedNumber: Long,
    val primaryCurrency: String
)

data class ScreenElement(
    val label: String,
    val value: String,
    val isPrimary: Boolean = false
)

object BundledScenarios {
    val ALL = listOf(
        TestScenario(
            id = "kirana_55",
            title = "Kirana Shop Checkout",
            contextCategory = "Local Grocery Store",
            contextDescription = "The shopkeeper packs 1 kg of sugar and says “Pachpan rupaye” to you.",
            spokenOrVisibleText = "Pachpan rupaye",
            screenElements = listOf(
                ScreenElement("Item", "Sugar 1kg"),
                ScreenElement("Total Amount", "₹55", isPrimary = true)
            ),
            primaryExpectedNumber = 55L,
            primaryCurrency = "₹"
        ),
        TestScenario(
            id = "restaurant_bill",
            title = "Restaurant Table Bill",
            contextCategory = "Dining & Bills",
            contextDescription = "A printed table receipt with multiple numbers: Table No. 24, Order #103, Total ₹340.",
            spokenOrVisibleText = "Table No. 24, Order #103, Total ₹340",
            screenElements = listOf(
                ScreenElement("Table No.", "24"),
                ScreenElement("Order No.", "103"),
                ScreenElement("Total Amount", "₹340", isPrimary = true)
            ),
            primaryExpectedNumber = 340L,
            primaryCurrency = "₹"
        ),
        TestScenario(
            id = "auto_fare",
            title = "Auto-Rickshaw Fare",
            contextCategory = "Transport & Travel",
            contextDescription = "The auto driver points to the digital meter and says “Sattavan rupaye”.",
            spokenOrVisibleText = "Sattavan rupaye",
            screenElements = listOf(
                ScreenElement("Distance", "3.2 km"),
                ScreenElement("Meter Fare", "₹57", isPrimary = true)
            ),
            primaryExpectedNumber = 57L,
            primaryCurrency = "₹"
        ),
        TestScenario(
            id = "sabzi_mandi",
            title = "Vegetable Market Price Board",
            contextCategory = "Market Prices",
            contextDescription = "A Hindi handwritten blackboard listing prices: प्याज ₹45, आलू ₹28, टमाटर ₹60.",
            spokenOrVisibleText = "प्याज ₹45, आलू ₹28, टमाटर ₹60",
            screenElements = listOf(
                ScreenElement("Onion (प्याज)", "₹45/kg"),
                ScreenElement("Potato (आलू)", "₹28/kg"),
                ScreenElement("Tomato (टमाटर)", "₹60/kg", isPrimary = true)
            ),
            primaryExpectedNumber = 60L,
            primaryCurrency = "₹"
        ),
        TestScenario(
            id = "train_ticket",
            title = "Train Seat & Fare",
            contextCategory = "Railway Ticket",
            contextDescription = "IRCTC confirmation showing Train #12951, Coach B3, Seat #78, Fare ₹1,485.",
            spokenOrVisibleText = "Train 12951, Seat 78, Fare ₹1485",
            screenElements = listOf(
                ScreenElement("Train Number", "12951"),
                ScreenElement("Seat Number", "78"),
                ScreenElement("Ticket Fare", "₹1,485", isPrimary = true)
            ),
            primaryExpectedNumber = 1485L,
            primaryCurrency = "₹"
        ),
        TestScenario(
            id = "upi_transfer",
            title = "UPI Payment Request",
            contextCategory = "Digital Payments",
            contextDescription = "Payment request of ₹850 with note: “Aath sau pachas rupaye”.",
            spokenOrVisibleText = "Aath sau pachas rupaye",
            screenElements = listOf(
                ScreenElement("Payee", "Ramesh Kumar"),
                ScreenElement("Amount", "₹850", isPrimary = true)
            ),
            primaryExpectedNumber = 850L,
            primaryCurrency = "₹"
        )
    )
}
