const data = {
    customers: [
        {
            name: "Ahmed Khan",
            contact: "082 123 4567",
            code: "BTT001",
            status: "Active"
        },
        {
            name: "Fatima Adams",
            contact: "083 222 1144",
            code: "BTT002",
            status: "Active"
        },
        {
            name: "Yusuf Daniels",
            contact: "084 555 7788",
            code: "BTT003",
            status: "Pending"
        },
        {
            name: "Amina Jacobs",
            contact: "081 445 9912",
            code: "BTT004",
            status: "Active"
        }
    ],

    applications: [
        {
            name: "Ahmed Khan",
            package: "Premium",
            date: "01 Sep 2026",
            status: "Pending"
        },
        {
            name: "Fatima Adams",
            package: "Standard",
            date: "02 Sep 2026",
            status: "Approved"
        },
        {
            name: "Yusuf Daniels",
            package: "Economy",
            date: "03 Sep 2026",
            status: "Under Review"
        },
        {
            name: "Amina Jacobs",
            package: "Premium",
            date: "04 Sep 2026",
            status: "Documents Required"
        }
    ],

    payments: [
        {
            name: "Ahmed Khan",
            amount: "R10,000",
            date: "01 Sep 2026",
            status: "Paid",
            balance: "R15,000"
        },
        {
            name: "Fatima Adams",
            amount: "R5,000",
            date: "02 Sep 2026",
            status: "Partial",
            balance: "R20,000"
        },
        {
            name: "Yusuf Daniels",
            amount: "R0",
            date: "03 Sep 2026",
            status: "Pending",
            balance: "R25,000"
        }
    ]
};

const pages = {
    dashboard: dashboardPage,
    customers: customersPage,
    applications: applicationsPage,
    documents: documentsPage,
    packages: packagesPage,
    bookings: bookingsPage,
    payments: paymentsPage,
    schedules: schedulesPage,
    reports: reportsPage,
    settings: settingsPage
};

function statusClass(status) {
    if (!status) {
        return "info";
    }

    const s = status.toLowerCase();

    if (
        s.includes("approved") ||
        s.includes("active") ||
        s.includes("paid") ||
        s.includes("confirmed") ||
        s.includes("verified") ||
        s.includes("available")
    ) {
        return "success";
    }

    if (
        s.includes("pending") ||
        s.includes("partial") ||
        s.includes("required") ||
        s.includes("review")
    ) {
        return "pending";
    }

    if (
        s.includes("reject") ||
        s.includes("cancel")
    ) {
        return "danger";
    }

    return "info";
}

function tableRows(items, type) {

    if (!items || items.length === 0) {
        return "";
    }

    if (type === "customers") {

        return items.map(customer => `
            <tr>
                <td>
                    <strong>${customer.name}</strong>
                </td>

                <td>${customer.contact}</td>

                <td>${customer.code}</td>

                <td>
                    <span class="status ${statusClass(customer.status)}">
                        ${customer.status}
                    </span>
                </td>

                <td>
                    <button
                        class="btn"
                        onclick="viewCustomer('${customer.name}')">
                        View
                    </button>

                    <button
                        class="btn"
                        onclick="showToast('Edit screen opened')">
                        Edit
                    </button>
                </td>
            </tr>
        `).join("");
    }


    if (type === "applications") {

        return items.map(application => `
            <tr>
                <td>
                    <strong>${application.name}</strong>
                </td>

                <td>${application.package}</td>

                <td>${application.date}</td>

                <td>
                    <span class="status ${statusClass(application.status)}">
                        ${application.status}
                    </span>
                </td>

                <td>
                    <button
                        class="btn"
                        onclick="reviewApplication('${application.name}')">
                        Review
                    </button>
                </td>
            </tr>
        `).join("");
    }


    if (type === "payments") {

        return items.map(payment => `
            <tr>
                <td>
                    <strong>${payment.name}</strong>
                </td>

                <td>${payment.amount}</td>

                <td>${payment.date}</td>

                <td>
                    <span class="status ${statusClass(payment.status)}">
                        ${payment.status}
                    </span>
                </td>

                <td>${payment.balance}</td>

                <td>
                    <button
                        class="btn"
                        onclick="showToast('Payment details opened')">
                        View
                    </button>
                </td>
            </tr>
        `).join("");
    }

    return "";
}

function dashboardPage() {

    return `
        <div class="page-heading">

            <div>
                <h2>Dashboard</h2>

                <p>
                    Overview of the Barakah Travel & Tours
                    Umrah management system.
                </p>
            </div>

        </div>


        <div class="stats">

            <div class="stat-card">

                <div class="stat-top">
                    <span>Total Customers</span>
                    <span>👥</span>
                </div>

                <div class="stat-value">
                    125
                </div>

                <div class="stat-note">
                    +8 this month
                </div>

            </div>


            <div class="stat-card">

                <div class="stat-top">
                    <span>Pending Applications</span>
                    <span>▤</span>
                </div>

                <div class="stat-value">
                    15
                </div>

                <div class="stat-note">
                    Requires attention
                </div>

            </div>


            <div class="stat-card">

                <div class="stat-top">
                    <span>Active Bookings</span>
                    <span>✈</span>
                </div>

                <div class="stat-value">
                    32
                </div>

                <div class="stat-note">
                    Upcoming journeys
                </div>

            </div>


            <div class="stat-card">

                <div class="stat-top">
                    <span>Outstanding Payments</span>
                    <span>R</span>
                </div>

                <div class="stat-value">
                    R50k
                </div>

                <div class="stat-note">
                    Monitor balances
                </div>

            </div>

        </div>


        <div class="grid-2">

            <section class="panel">

                <div class="panel-title">

                    <h3>
                        Recent Applications
                    </h3>

                    <a
                        href="#"
                        onclick="navigate('applications'); return false;">
                        View all
                    </a>

                </div>


                <div class="table-wrap">

                    <table>

                        <thead>

                            <tr>
                                <th>Applicant</th>
                                <th>Package</th>
                                <th>Date</th>
                                <th>Status</th>
                            </tr>

                        </thead>


                        <tbody>

                            ${data.applications
                                .slice(0, 3)
                                .map(application => `
                                    <tr>

                                        <td>
                                            <strong>
                                                ${application.name}
                                            </strong>
                                        </td>

                                        <td>
                                            ${application.package}
                                        </td>

                                        <td>
                                            ${application.date}
                                        </td>

                                        <td>
                                            <span class="status ${statusClass(application.status)}">
                                                ${application.status}
                                            </span>
                                        </td>

                                    </tr>
                                `)
                                .join("")}

                        </tbody>

                    </table>

                </div>

            </section>


            <section class="panel">

                <div class="panel-title">
                    <h3>Quick Actions</h3>
                </div>


                <div class="quick-actions">

                    <button onclick="openAddCustomer()">
                        ＋ Add Customer
                    </button>

                    <button onclick="openPackageForm()">
                        ✈ Add Umrah Package
                    </button>

                    <button onclick="openBookingForm()">
                        ＋ Create Booking
                    </button>

                    <button onclick="openPaymentForm()">
                        R Record Payment
                    </button>

                </div>

            </section>

        </div>


        <section class="panel">

            <div class="panel-title">

                <h3>
                    Upcoming Travel Schedule
                </h3>

                <a
                    href="#"
                    onclick="navigate('schedules'); return false;">
                    View schedules
                </a>

            </div>


            <div class="table-wrap">

                <table>

                    <thead>

                        <tr>
                            <th>Departure</th>
                            <th>Return</th>
                            <th>Package</th>
                            <th>Spaces</th>
                            <th>Status</th>
                        </tr>

                    </thead>


                    <tbody>

                        <tr>
                            <td>15 Dec 2026</td>
                            <td>29 Dec 2026</td>
                            <td>Premium</td>
                            <td>10</td>

                            <td>
                                <span class="status success">
                                    Available
                                </span>
                            </td>
                        </tr>


                        <tr>
                            <td>20 Dec 2026</td>
                            <td>03 Jan 2027</td>
                            <td>Standard</td>
                            <td>5</td>

                            <td>
                                <span class="status success">
                                    Available
                                </span>
                            </td>
                        </tr>

                    </tbody>

                </table>

            </div>

        </section>
    `;
}

function customersPage() {

    return `
        <div class="page-heading">

            <div>

                <h2>Customers</h2>

                <p>
                    Register, search and manage customer information.
                </p>

            </div>


            <div class="actions">

                <button
                    class="btn primary"
                    onclick="openAddCustomer()">
                    ＋ Add Customer
                </button>


                <button
                    class="btn gold"
                    onclick="showToast('Registration code generated: BTT005')">
                    Generate Registration Code
                </button>

            </div>

        </div>


        <section class="panel">

            <div class="search-row">

                <input
                    id="customerSearch"
                    type="text"
                    placeholder="Search customers by name or code..."
                    onkeyup="handleCustomerSearch(event)">


                <button
                    class="btn primary"
                    onclick="filterCustomers()">
                    Search
                </button>

            </div>


            <div class="table-wrap">

                <table>

                    <thead>

                        <tr>
                            <th>Name</th>
                            <th>Contact</th>
                            <th>Registration Code</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>

                    </thead>


                    <tbody id="customerRows">

                        ${tableRows(data.customers, "customers")}

                    </tbody>

                </table>

            </div>

        </section>
    `;
}

function applicationsPage() {

    return `
        <div class="page-heading">

            <div>

                <h2>Applications</h2>

                <p>
                    Review Umrah applications and monitor their status.
                </p>

            </div>

        </div>


        <section class="panel">

            <div class="search-row">

                <input
                    id="applicationSearch"
                    type="text"
                    placeholder="Search applications...">


                <select id="applicationStatus">

                    <option value="">
                        All Statuses
                    </option>

                    <option value="Pending">
                        Pending
                    </option>

                    <option value="Approved">
                        Approved
                    </option>

                    <option value="Under Review">
                        Under Review
                    </option>

                    <option value="Documents Required">
                        Documents Required
                    </option>

                </select>


                <button
                    class="btn primary"
                    onclick="filterApplications()">
                    Search
                </button>

            </div>


            <div class="table-wrap">

                <table>

                    <thead>

                        <tr>
                            <th>Applicant</th>
                            <th>Package</th>
                            <th>Application Date</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>

                    </thead>


                    <tbody id="applicationRows">

                        ${tableRows(data.applications, "applications")}

                    </tbody>

                </table>

            </div>

        </section>
    `;
}


// ============================================================
// DOCUMENTS PAGE
// ============================================================

function documentsPage() {

    const docs = [
        ["Ahmed Khan", "Passport", "01 Sep 2026", "Verified"],
        ["Ahmed Khan", "ID Document", "01 Sep 2026", "Verified"],
        ["Fatima Adams", "Proof of Payment", "02 Sep 2026", "Pending"],
        ["Yusuf Daniels", "Passport", "03 Sep 2026", "Pending"]
    ];


    return `
        <div class="page-heading">

            <div>

                <h2>
                    Document Verification
                </h2>

                <p>
                    Review and verify documents submitted
                    with applications.
                </p>

            </div>

        </div>


        <section class="panel">

            <div class="table-wrap">

                <table>

                    <thead>

                        <tr>
                            <th>Customer</th>
                            <th>Document</th>
                            <th>Uploaded</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>

                    </thead>


                    <tbody>

                        ${docs.map(document => `

                            <tr>

                                <td>
                                    ${document[0]}
                                </td>

                                <td>
                                    ${document[1]}
                                </td>

                                <td>
                                    ${document[2]}
                                </td>

                                <td>

                                    <span class="status ${statusClass(document[3])}">
                                        ${document[3]}
                                    </span>

                                </td>


                                <td>

                                    <button
                                        class="btn"
                                        onclick="showToast('Document preview opened')">
                                        View
                                    </button>


                                    ${
                                        document[3] !== "Verified"
                                            ? `
                                                <button
                                                    class="btn primary"
                                                    onclick="showToast('Document verified')">
                                                    Verify
                                                </button>
                                            `
                                            : ""
                                    }

                                </td>

                            </tr>

                        `).join("")}

                    </tbody>

                </table>

            </div>

        </section>
    `;
}

function packagesPage() {

    const packages = [
        [
            "Economy",
            "10 Days",
            "R25,000",
            "Basic accommodation and transport"
        ],
        [
            "Standard",
            "14 Days",
            "R35,000",
            "Comfort accommodation, transport and meals"
        ],
        [
            "Premium",
            "21 Days",
            "R50,000",
            "Premium accommodation, transport and guided support"
        ]
    ];


    return `
        <div class="page-heading">

            <div>

                <h2>
                    Umrah Packages
                </h2>

                <p>
                    Manage packages offered to customers.
                </p>

            </div>


            <button
                class="btn primary"
                onclick="openPackageForm()">
                ＋ Add Package
            </button>

        </div>


        <div class="package-grid">

            ${packages.map(packageData => `

                <div class="package-card">

                    <span class="status success">
                        Active
                    </span>


                    <h3>
                        ${packageData[0]} Package
                    </h3>


                    <p>
                        ${packageData[1]}
                    </p>


                    <div class="price">
                        ${packageData[2]}
                    </div>


                    <ul>

                        <li>
                            ✓ ${packageData[3]}
                        </li>

                        <li>
                            ✓ Travel schedule included
                        </li>

                        <li>
                            ✓ Application support
                        </li>

                    </ul>


                    <button
                        class="btn"
                        onclick="showToast('Package details opened')">
                        View Details
                    </button>


                    <button
                        class="btn"
                        onclick="showToast('Edit package screen opened')">
                        Edit
                    </button>

                </div>

            `).join("")}

        </div>
    `;
}


// ============================================================
// BOOKINGS PAGE
// ============================================================

function bookingsPage() {

    return `
        <div class="page-heading">

            <div>

                <h2>
                    Bookings
                </h2>

                <p>
                    Create bookings and manage booking status.
                </p>

            </div>


            <button
                class="btn primary"
                onclick="openBookingForm()">
                ＋ Create Booking
            </button>

        </div>


        <section class="panel">

            <div class="search-row">

                <input
                    id="bookingSearch"
                    type="text"
                    placeholder="Search bookings...">


                <button
                    class="btn primary"
                    onclick="showToast('Booking search completed')">
                    Search
                </button>

            </div>


            <div class="table-wrap">

                <table>

                    <thead>

                        <tr>
                            <th>Customer</th>
                            <th>Package</th>
                            <th>Travel Date</th>
                            <th>Payment</th>
                            <th>Booking Status</th>
                            <th>Action</th>
                        </tr>

                    </thead>


                    <tbody>

                        <tr>

                            <td>
                                Ahmed Khan
                            </td>

                            <td>
                                Premium
                            </td>

                            <td>
                                15 Dec 2026
                            </td>

                            <td>
                                <span class="status pending">
                                    Partial
                                </span>
                            </td>

                            <td>
                                <span class="status success">
                                    Confirmed
                                </span>
                            </td>

                            <td>
                                <button
                                    class="btn"
                                    onclick="showToast('Booking details opened')">
                                    View
                                </button>
                            </td>

                        </tr>


                        <tr>

                            <td>
                                Fatima Adams
                            </td>

                            <td>
                                Standard
                            </td>

                            <td>
                                20 Dec 2026
                            </td>

                            <td>
                                <span class="status success">
                                    Paid
                                </span>
                            </td>

                            <td>
                                <span class="status pending">
                                    Pending
                                </span>
                            </td>

                            <td>
                                <button
                                    class="btn"
                                    onclick="showToast('Booking details opened')">
                                    View
                                </button>
                            </td>

                        </tr>

                    </tbody>

                </table>

            </div>

        </section>
    `;
}

function paymentsPage() {

    return `
        <div class="page-heading">

            <div>

                <h2>
                    Payments
                </h2>

                <p>
                    Record payments and monitor outstanding balances.
                </p>

            </div>


            <button
                class="btn primary"
                onclick="openPaymentForm()">
                ＋ Record Payment
            </button>

        </div>


        <section class="panel">

            <div class="search-row">

                <input
                    type="text"
                    placeholder="Search customer or payment...">


                <button
                    class="btn primary"
                    onclick="showToast('Payment search completed')">
                    Search
                </button>

            </div>


            <div class="table-wrap">

                <table>

                    <thead>

                        <tr>
                            <th>Customer</th>
                            <th>Amount</th>
                            <th>Date</th>
                            <th>Status</th>
                            <th>Balance</th>
                            <th>Action</th>
                        </tr>

                    </thead>


                    <tbody>

                        ${tableRows(data.payments, "payments")}

                    </tbody>

                </table>

            </div>

        </section>
    `;
}

function schedulesPage() {

    return `
        <div class="page-heading">

            <div>

                <h2>
                    Travel Schedules
                </h2>

                <p>
                    Manage Umrah departure and return schedules.
                </p>

            </div>


            <button
                class="btn primary"
                onclick="openScheduleForm()">
                ＋ Add Schedule
            </button>

        </div>


        <section class="panel">

            <div class="table-wrap">

                <table>

                    <thead>

                        <tr>
                            <th>Departure</th>
                            <th>Return</th>
                            <th>Package</th>
                            <th>Departure Location</th>
                            <th>Spaces</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>

                    </thead>


                    <tbody>

                        <tr>

                            <td>
                                15 Dec 2026
                            </td>

                            <td>
                                29 Dec 2026
                            </td>

                            <td>
                                Premium
                            </td>

                            <td>
                                Cape Town
                            </td>

                            <td>
                                10
                            </td>

                            <td>
                                <span class="status success">
                                    Available
                                </span>
                            </td>

                            <td>

                                <button
                                    class="btn"
                                    onclick="showToast('Schedule edit screen opened')">
                                    Edit
                                </button>

                            </td>

                        </tr>


                        <tr>

                            <td>
                                20 Dec 2026
                            </td>

                            <td>
                                03 Jan 2027
                            </td>

                            <td>
                                Standard
                            </td>

                            <td>
                                Cape Town
                            </td>

                            <td>
                                5
                            </td>

                            <td>
                                <span class="status success">
                                    Available
                                </span>
                            </td>

                            <td>

                                <button
                                    class="btn"
                                    onclick="showToast('Schedule edit screen opened')">
                                    Edit
                                </button>

                            </td>

                        </tr>

                    </tbody>

                </table>

            </div>

        </section>
    `;
}

function reportsPage() {

    return `
        <div class="page-heading">

            <div>

                <h2>
                    Reports
                </h2>

                <p>
                    Generate summaries for customers,
                    applications and payments.
                </p>

            </div>

        </div>


        <section class="report-grid">

            <div class="report-card">

                <h3>
                    Customer Reports
                </h3>

                <p>
                    View customer totals, new registrations
                    and active customers.
                </p>

                <button
                    class="btn primary"
                    onclick="showToast('Customer report generated')">
                    Generate Report
                </button>

            </div>


            <div class="report-card">

                <h3>
                    Application Reports
                </h3>

                <p>
                    Review pending, approved, rejected
                    and document-required applications.
                </p>

                <button
                    class="btn primary"
                    onclick="showToast('Application report generated')">
                    Generate Report
                </button>

            </div>


            <div class="report-card">

                <h3>
                    Payment Reports
                </h3>

                <p>
                    Review payments received and outstanding
                    customer balances.
                </p>

                <button
                    class="btn primary"
                    onclick="showToast('Payment report generated')">
                    Generate Report
                </button>

            </div>

        </section>


        <section class="panel">

            <div class="panel-title">

                <h3>
                    Report Date Range
                </h3>

            </div>


            <div class="form-grid">

                <div class="form-group">

                    <label>
                        From
                    </label>

                    <input
                        type="date"
                        id="reportFrom">

                </div>


                <div class="form-group">

                    <label>
                        To
                    </label>

                    <input
                        type="date"
                        id="reportTo">

                </div>

            </div>

        </section>
    `;
}

function settingsPage() {

    return `
        <div class="page-heading">

            <div>

                <h2>
                    Profile / Settings
                </h2>

                <p>
                    Manage your account information and preferences.
                </p>

            </div>

        </div>


        <section class="panel">

            <div class="form-grid">

                <div class="form-group">

                    <label>
                        Full Name
                    </label>

                    <input
                        value="Admin User">

                </div>


                <div class="form-group">

                    <label>
                        Email Address
                    </label>

                    <input
                        type="email"
                        value="admin@barakahtravel.example">

                </div>


                <div class="form-group">

                    <label>
                        Phone Number
                    </label>

                    <input
                        value="082 000 0000">

                </div>


                <div class="form-group">

                    <label>
                        Role
                    </label>

                    <select>

                        <option>
                            Administrator
                        </option>

                        <option>
                            Staff
                        </option>

                    </select>

                </div>


                <div class="form-group full">

                    <label>
                        Notification Preferences
                    </label>

                    <label>

                        <input
                            type="checkbox"
                            checked>

                        Receive application and payment notifications

                    </label>

                </div>

            </div>


            <div class="form-actions">

                <button
                    class="btn primary"
                    onclick="showToast('Settings saved successfully')">
                    Save Changes
                </button>

            </div>

        </section>
    `;
}

function openAddCustomer() {

    openModal(`
        <h2>
            Add Customer
        </h2>

        <p style="color:#6c7773;margin:6px 0 20px">
            Enter the customer's registration details.
        </p>


        <form
            onsubmit="event.preventDefault(); saveCustomer();">

            <div class="form-grid">

                <div class="form-group">

                    <label>
                        First Name
                    </label>

                    <input
                        id="customerFirstName"
                        required>

                </div>


                <div class="form-group">

                    <label>
                        Surname
                    </label>

                    <input
                        id="customerSurname"
                        required>

                </div>


                <div class="form-group">

                    <label>
                        Date of Birth
                    </label>

                    <input
                        type="date">

                </div>


                <div class="form-group">

                    <label>
                        Gender
                    </label>

                    <select>

                        <option value="">
                            Select
                        </option>

                        <option>
                            Male
                        </option>

                        <option>
                            Female
                        </option>

                    </select>

                </div>


                <div class="form-group full">

                    <label>
                        ID / Passport Number
                    </label>

                    <input
                        required>

                </div>


                <div class="form-group">

                    <label>
                        Phone Number
                    </label>

                    <input
                        required>

                </div>


                <div class="form-group">

                    <label>
                        Email Address
                    </label>

                    <input
                        type="email">

                </div>


                <div class="form-group full">

                    <label>
                        Residential Address
                    </label>

                    <input>

                </div>


                <div class="form-group full">

                    <label>
                        Emergency Contact
                    </label>

                    <input>

                </div>

            </div>


            <div class="form-actions">

                <button
                    type="button"
                    class="btn"
                    onclick="closeModal()">
                    Cancel
                </button>


                <button
                    type="submit"
                    class="btn primary">
                    Save Customer
                </button>

            </div>

        </form>
    `);
}

function saveCustomer() {

    const firstName =
        document.getElementById("customerFirstName")?.value.trim();

    const surname =
        document.getElementById("customerSurname")?.value.trim();


    if (!firstName || !surname) {

        showToast("Please enter the customer's name.");

        return;
    }


    const fullName = `${firstName} ${surname}`;

    const newCode =
        `BTT${String(data.customers.length + 1).padStart(3, "0")}`;


    data.customers.push({
        name: fullName,
        contact: "Not provided",
        code: newCode,
        status: "Active"
    });


    closeModal();

    showToast(
        `Customer added successfully. Registration code: ${newCode}`
    );

    const currentPage =
        document.querySelector(".nav-link.active")?.dataset.page;

    if (currentPage === "customers") {
        navigate("customers");
    }
}

function openPackageForm() {

    openModal(`

        <h2>
            Add Umrah Package
        </h2>


        <form
            onsubmit="event.preventDefault(); savePackage();">

            <div class="form-grid">

                <div class="form-group full">

                    <label>
                        Package Name
                    </label>

                    <input
                        required>

                </div>


                <div class="form-group">

                    <label>
                        Duration
                    </label>

                    <input
                        placeholder="e.g. 14 days">

                </div>


                <div class="form-group">

                    <label>
                        Price
                    </label>

                    <input
                        placeholder="R35,000">

                </div>


                <div class="form-group full">

                    <label>
                        Description
                    </label>

                    <textarea></textarea>

                </div>


                <div class="form-group">

                    <label>
                        Accommodation
                    </label>

                    <input>

                </div>


                <div class="form-group">

                    <label>
                        Transport
                    </label>

                    <input>

                </div>


                <div class="form-group">

                    <label>
                        Meals
                    </label>

                    <input>

                </div>


                <div class="form-group">

                    <label>
                        Status
                    </label>

                    <select>

                        <option>
                            Active
                        </option>

                        <option>
                            Inactive
                        </option>

                    </select>

                </div>

            </div>


            <div class="form-actions">

                <button
                    type="button"
                    class="btn"
                    onclick="closeModal()">
                    Cancel
                </button>


                <button
                    type="submit"
                    class="btn primary">
                    Save Package
                </button>

            </div>

        </form>
    `);
}


function savePackage() {

    closeModal();

    showToast(
        "Package saved successfully"
    );
}

function openBookingForm() {

    openModal(`

        <h2>
            Create Booking
        </h2>


        <form
            onsubmit="event.preventDefault(); saveBooking();">

            <div class="form-grid">

                <div class="form-group">

                    <label>
                        Customer
                    </label>

                    <select>

                        <option>
                            Ahmed Khan
                        </option>

                        <option>
                            Fatima Adams
                        </option>

                        <option>
                            Yusuf Daniels
                        </option>

                        <option>
                            Amina Jacobs
                        </option>

                    </select>

                </div>


                <div class="form-group">

                    <label>
                        Package
                    </label>

                    <select>

                        <option>
                            Premium
                        </option>

                        <option>
                            Standard
                        </option>

                        <option>
                            Economy
                        </option>

                    </select>

                </div>


                <div class="form-group">

                    <label>
                        Travel Date
                    </label>

                    <input
                        type="date"
                        required>

                </div>


                <div class="form-group">

                    <label>
                        Number of Travellers
                    </label>

                    <input
                        type="number"
                        min="1"
                        value="1"
                        required>

                </div>


                <div class="form-group full">

                    <label>
                        Notes
                    </label>

                    <textarea></textarea>

                </div>

            </div>


            <div class="form-actions">

                <button
                    type="button"
                    class="btn"
                    onclick="closeModal()">
                    Cancel
                </button>


                <button
                    type="submit"
                    class="btn primary">
                    Create Booking
                </button>

            </div>

        </form>
    `);
}


function saveBooking() {

    closeModal();

    showToast(
        "Booking created successfully"
    );
}


// ============================================================
// RECORD PAYMENT MODAL
// ============================================================

function openPaymentForm() {

    openModal(`

        <h2>
            Record Payment
        </h2>


        <form
            onsubmit="event.preventDefault(); savePayment();">

            <div class="form-grid">

                <div class="form-group">

                    <label>
                        Customer
                    </label>

                    <select>

                        <option>
                            Ahmed Khan
                        </option>

                        <option>
                            Fatima Adams
                        </option>

                        <option>
                            Yusuf Daniels
                        </option>

                        <option>
                            Amina Jacobs
                        </option>

                    </select>

                </div>


                <div class="form-group">

                    <label>
                        Amount
                    </label>

                    <input
                        type="number"
                        min="0"
                        step="0.01"
                        placeholder="10000"
                        required>

                </div>


                <div class="form-group">

                    <label>
                        Payment Date
                    </label>

                    <input
                        type="date"
                        required>

                </div>


                <div class="form-group">

                    <label>
                        Payment Method
                    </label>

                    <select>

                        <option>
                            Bank Transfer
                        </option>

                        <option>
                            Cash
                        </option>

                        <option>
                            Card
                        </option>

                    </select>

                </div>

            </div>


            <div class="form-actions">

                <button
                    type="button"
                    class="btn"
                    onclick="closeModal()">
                    Cancel
                </button>


                <button
                    type="submit"
                    class="btn primary">
                    Record Payment
                </button>

            </div>

        </form>
    `);
}


function savePayment() {

    closeModal();

    showToast(
        "Payment recorded successfully"
    );
}

function openScheduleForm() {

    openModal(`

        <h2>
            Add Travel Schedule
        </h2>


        <form
            onsubmit="event.preventDefault(); saveSchedule();">

            <div class="form-grid">

                <div class="form-group">

                    <label>
                        Departure Date
                    </label>

                    <input
                        type="date"
                        required>

                </div>


                <div class="form-group">

                    <label>
                        Return Date
                    </label>

                    <input
                        type="date"
                        required>

                </div>


                <div class="form-group">

                    <label>
                        Package
                    </label>

                    <select>

                        <option>
                            Premium
                        </option>

                        <option>
                            Standard
                        </option>

                        <option>
                            Economy
                        </option>

                    </select>

                </div>


                <div class="form-group">

                    <label>
                        Departure Location
                    </label>

                    <input
                        value="Cape Town">

                </div>


                <div class="form-group">

                    <label>
                        Available Spaces
                    </label>

                    <input
                        type="number"
                        min="1"
                        required>

                </div>


                <div class="form-group">

                    <label>
                        Status
                    </label>

                    <select>

                        <option>
                            Available
                        </option>

                        <option>
                            Full
                        </option>

                        <option>
                            Cancelled
                        </option>

                    </select>

                </div>

            </div>


            <div class="form-actions">

                <button
                    type="button"
                    class="btn"
                    onclick="closeModal()">
                    Cancel
                </button>


                <button
                    type="submit"
                    class="btn primary">
                    Save Schedule
                </button>

            </div>

        </form>
    `);
}


function saveSchedule() {

    closeModal();

    showToast(
        "Travel schedule added successfully"
    );
}

function viewCustomer(name) {

    const customer =
        data.customers.find(
            customer => customer.name === name
        );


    if (!customer) {

        showToast(
            "Customer could not be found."
        );

        return;
    }


    openModal(`

        <h2>
            Customer Details
        </h2>


        <div
            class="panel"
            style="margin-top:18px;box-shadow:none">

            <p>
                <strong>Name:</strong>
                ${customer.name}
            </p>


            <p style="margin-top:10px">

                <strong>
                    Contact:
                </strong>

                ${customer.contact}

            </p>


            <p style="margin-top:10px">

                <strong>
                    Registration Code:
                </strong>

                ${customer.code}

            </p>


            <p style="margin-top:10px">

                <strong>
                    Status:
                </strong>

                <span class="status ${statusClass(customer.status)}">
                    ${customer.status}
                </span>

            </p>

        </div>
    `);
}

function reviewApplication(name) {

    const application =
        data.applications.find(
            item => item.name === name
        );


    if (!application) {

        showToast(
            "Application could not be found."
        );

        return;
    }


    openModal(`

        <h2>
            Application Review
        </h2>


        <p
            style="color:#6c7773;margin:7px 0 18px">

            Reviewing application for
            <strong>${name}</strong>.

        </p>


        <div
            class="panel"
            style="box-shadow:none">

            <p>
                <strong>
                    Application information
                </strong>
            </p>


            <p style="margin-top:10px">

                Selected Umrah package:
                <strong>
                    ${application.package}
                </strong>

            </p>


            <p style="margin-top:10px">

                Application date:
                ${application.date}

            </p>


            <p style="margin-top:10px">

                Current status:
                <span class="status ${statusClass(application.status)}">
                    ${application.status}
                </span>

            </p>


            <p style="margin-top:10px">

                Documents:
                Passport ✓ &nbsp;
                ID ✓ &nbsp;
                Proof of payment Pending

            </p>

        </div>


        <div class="form-group">

            <label>
                Review Comments
            </label>

            <textarea
                id="reviewComments"
                placeholder="Add review notes..."></textarea>

        </div>


        <div class="form-actions">

            <button
                class="btn danger"
                onclick="rejectApplication('${name}')">
                Reject
            </button>


            <button
                class="btn gold"
                onclick="requestDocuments('${name}')">
                Request Documents
            </button>


            <button
                class="btn primary"
                onclick="approveApplication('${name}')">
                Approve
            </button>

        </div>
    `);
}

function approveApplication(name) {

    const application =
        data.applications.find(
            item => item.name === name
        );


    if (application) {

        application.status = "Approved";

    }


    closeModal();

    showToast(
        "Application approved"
    );
}


function rejectApplication(name) {

    const application =
        data.applications.find(
            item => item.name === name
        );


    if (application) {

        application.status = "Rejected";

    }


    closeModal();

    showToast(
        "Application rejected"
    );
}


function requestDocuments(name) {

    const application =
        data.applications.find(
            item => item.name === name
        );


    if (application) {

        application.status = "Documents Required";

    }


    closeModal();

    showToast(
        "Additional documents requested"
    );
}

function filterCustomers() {

    const searchInput =
        document.getElementById("customerSearch");


    if (!searchInput) {
        return;
    }


    const query =
        searchInput.value
            .toLowerCase()
            .trim();


    const filtered =
        data.customers.filter(customer =>
            `${customer.name} ${customer.code} ${customer.contact}`
                .toLowerCase()
                .includes(query)
        );


    const customerRows =
        document.getElementById("customerRows");


    if (!customerRows) {
        return;
    }


    customerRows.innerHTML =
        tableRows(filtered, "customers") ||
        `
            <tr>
                <td
                    colspan="5"
                    class="empty">
                    No customers found.
                </td>
            </tr>
        `;
}


function handleCustomerSearch(event) {

    if (event.key === "Enter") {
        filterCustomers();
    }
}

function filterApplications() {

    const searchInput =
        document.getElementById("applicationSearch");

    const statusInput =
        document.getElementById("applicationStatus");


    const query =
        searchInput
            ? searchInput.value.toLowerCase().trim()
            : "";


    const selectedStatus =
        statusInput
            ? statusInput.value
            : "";


    const filtered =
        data.applications.filter(application => {

            const matchesSearch =
                `${application.name} ${application.package} ${application.date}`
                    .toLowerCase()
                    .includes(query);


            const matchesStatus =
                !selectedStatus ||
                application.status === selectedStatus;


            return matchesSearch && matchesStatus;
        });


    const applicationRows =
        document.getElementById("applicationRows");


    if (!applicationRows) {
        return;
    }


    applicationRows.innerHTML =
        tableRows(filtered, "applications") ||
        `
            <tr>
                <td
                    colspan="5"
                    class="empty">
                    No applications found.
                </td>
            </tr>
        `;
}

function openModal(content) {

    const modalContent =
        document.getElementById("modalContent");

    const modal =
        document.getElementById("modal");


    if (!modalContent || !modal) {

        console.error(
            "Modal elements were not found in the HTML."
        );

        return;
    }


    modalContent.innerHTML = content;

    modal.classList.add("show");

    document.body.classList.add("modal-open");
}


function closeModal() {

    const modal =
        document.getElementById("modal");


    if (!modal) {
        return;
    }


    modal.classList.remove("show");

    document.body.classList.remove("modal-open");
}

function showToast(message) {

    const toast =
        document.getElementById("toast");


    if (!toast) {

        console.log(message);

        return;
    }


    toast.textContent = message;

    toast.classList.add("show");


    clearTimeout(window.toastTimer);


    window.toastTimer =
        setTimeout(() => {

            toast.classList.remove("show");

        }, 2500);
}

function navigate(page) {

    if (!pages[page]) {

        console.error(
            `Page "${page}" does not exist.`
        );

        return;
    }


    const pageContent =
        document.getElementById("pageContent");


    if (!pageContent) {

        console.error(
            "The #pageContent element was not found."
        );

        return;
    }


    document
        .querySelectorAll(".nav-link")
        .forEach(button => {

            button.classList.toggle(
                "active",
                button.dataset.page === page
            );

        });


    pageContent.innerHTML =
        pages[page]();


    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });
}

function logout() {

    showToast(
        "Demo logout selected"
    );
}

function initialiseApp() {

    document
        .querySelectorAll(".nav-link")
        .forEach(button => {

            button.addEventListener(
                "click",
                function () {

                    const page =
                        this.dataset.page;

                    if (page) {
                        navigate(page);
                    }

                }
            );

        });


    const closeModalButton =
        document.getElementById("closeModal");


    if (closeModalButton) {

        closeModalButton.addEventListener(
            "click",
            closeModal
        );

    }

    const modal =
        document.getElementById("modal");


    if (modal) {

        modal.addEventListener(
            "click",
            function (event) {

                if (event.target === modal) {
                    closeModal();
                }

            }
        );

    }

    const logoutButton =
        document.getElementById("logoutBtn");


    if (logoutButton) {

        logoutButton.addEventListener(
            "click",
            logout
        );

    }

    navigate("dashboard");
}

if (document.readyState === "loading") {

    document.addEventListener(
        "DOMContentLoaded",
        initialiseApp
    );

} else {

    initialiseApp();

}