<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="model.Customer" %>
<%
    // Get customer object from request (used to render user profile)
    Customer customer = (Customer) request.getAttribute("customer");
    if (customer == null) {
        customer = new Customer();
    }
%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta charset="UTF-8">
        <title>${requestScope.i18n['screen.dbbe59230e6703c7']}</title>

        <!-- Bootstrap & Custom CSS -->
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/profile.css">

        <!-- FontAwesome Icons -->
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-modern.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-shared-shell.css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/luxe-ui.css" />
    </head>

    <body>

        <%@include file="/WEB-INF/include/header.jsp" %>

        <div class="profile-container">
            <!-- Left Sidebar for Avatar and Basic Info -->
            <div class="profile-sidebar glass-card">
                <form action="<%= request.getContextPath()%>/avatar" method="post" enctype="multipart/form-data">
                    <!-- Display avatar or default image -->

                    <%
                        String avatar = customer.getAvatar();

                        boolean hasAvatar = avatar != null
                                && !avatar.trim().isEmpty();
                    %>

                    <img
                        src="<%= request.getContextPath()
                                + (hasAvatar
                                        ? "/avatar/image"
                                        : "/assets/img/avatar.jpg")%>"
                        alt="Avatar"
                        class="img-thumbnail"
                        width="150"
                        height="150"
                        style="object-fit: cover; border-radius: 50%;"
                        >


                    <!-- Upload avatar trigger -->
                    <c:if test="${sessionScope.loggedUser.role == 'customer'}"><label for="avatarFile" class="upload-label">
                        <i class="fas fa-camera upload-icon"></i>
                        <span><ui:text key="screen.8c58ac4a340f9aad"/></span>
                    </label></c:if>
                    <input type="file" id="avatarFile" name="img" accept=".png, .jpg, .jpeg" class="d-none">
                    <div id="avatarError" class="small text-danger mt-2 d-none" role="alert">
                    </div>
                </form>

                <!-- Display customer name and email -->

                <h4>
                    ${fn:escapeXml(customer.firstName)}
                    ${fn:escapeXml(customer.lastName)}
                </h4>

                <p>${fn:escapeXml(customer.email)}</p>


                <div class="account-section">
                    <button class="account-btn active"><ui:text key="screen.3dae037ca0c24971"/></button>
                </div>
            </div>

            <!-- Main Profile Form Area -->
            <div class="profile-main glass-card">

                <%
                    String success = request.getParameter("success");
                    String error = request.getParameter("error");

                    if ("updated".equals(success)) {
                %>
                <div class="alert alert-success" role="alert">
                    Cập nhật hồ sơ thành công.
                </div>
                <%
                } else if (error != null) {
                    String message = "Unable to update profile.";

                    switch (error) {
                        case "invalid-name":
                            message = "Display name is invalid.";
                            break;
                        case "invalid-email":
                            message = "Please enter a valid email.";
                            break;
                        case "update-failed":
                            message = "Update failed. No changes were saved.";
                            break;

                        case "avatar-too-large":
                            message = "Image must not exceed 5 MB.";
                            break;

                        case "avatar-invalid-request":
                            message = "Invalid upload request. Please try again.";
                            break;

                    }
                %>
                <div class="alert alert-danger" role="alert">
                    <%= message%>
                </div>
                <%
                    }
                %>

                <form id="form-1" method="post" action="<%= request.getContextPath()%>/profile">
<input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                    <!-- Personal Details Section -->
                    <div class="form-section">
                        <h5><ui:text key="screen.3a886778636502fa"/></h5>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label"><ui:text key="screen.e3b89e9d33f88e52"/></label>
                                <input type="text" class="form-control" name="username" value="${fn:escapeXml(loggedUser.username)}" readonly>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label"><ui:text key="screen.18d67c992b71ce69"/></label>
                                <input type="text"
                                       class="form-control input_type"
                                       name="displayName"
                                       value="${fn:escapeXml(loggedUser.displayName)}"
                                       maxlength="100"
                                       required
                                       readonly>
                            </div>
                        </div>

                        <div class="row g-3 mt-3">
                            <div class="col-md-6">
                                <label class="form-label"><ui:text key="screen.96ce6e6193a04726"/></label>
                                <input type="text" class="form-control input_type" name="firstName" value="${fn:escapeXml(customer.firstName)}" readonly>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label"><ui:text key="screen.f55563b386a39dc3"/></label>
                                <input type="text" class="form-control input_type" name="lastName" value="${fn:escapeXml(customer.lastName)}" readonly>
                            </div>
                        </div>

                        <div class="row g-3 mt-3">
                            <div class="col-md-6">
                                <label class="form-label"><ui:text key="screen.969ccbd3cf6300ec"/></label>
                                <input type="email" class="form-control input_type" name="email" value="${fn:escapeXml(customer.email)}" readonly>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label"><ui:text key="screen.63dceb8800b2dd4a"/></label>
                                <input type="text" class="form-control input_type" name="phone" value="${fn:escapeXml(customer.phone)}" readonly>
                            </div>
                        </div>
                    </div>

                    <!-- Address Section -->
                    <div class="form-section">
                        <h5><ui:text key="screen.56ef8f20955f2564"/></h5>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label"><ui:text key="screen.701d021d08c54579"/></label>

                                <select id="country"
                                        name="country"
                                        class="form-control input_type"
                                        data-saved-country="${fn:escapeXml(customer.country)}"
                                        disabled>
                                    <option value="${fn:escapeXml(customer.country)}"
                                            selected>
                                        ${empty customer.country
                                          ? '-- Select Country --'
                                          : fn:escapeXml(customer.country)}
                                    </option>
                                </select>

                            </div>
                            <div class="col-md-6">
                                <label class="form-label"><ui:text key="screen.fc33f73246f48295"/></label>

                                <select id="city"
                                        name="city"
                                        class="form-control input_type"
                                        data-saved-city="${fn:escapeXml(customer.city)}"
                                        disabled>
                                    <option value="${fn:escapeXml(customer.city)}"
                                            selected>
                                        ${empty customer.city
                                          ? '-- Select City --'
                                          : fn:escapeXml(customer.city)}
                                    </option>
                                </select>

                                <div id="locationError"
                                     class="small text-danger mt-2 d-none"
                                     role="alert">
                                </div>

                            </div>
                        </div>

                        <div class="row g-3 mt-3">
                            <div class="col-12">
                                <label class="form-label"><ui:text key="screen.c666591df0537cf2"/></label>
                                <input type="text" class="form-control input_type" name="street" value="${fn:escapeXml(customer.street)}" readonly>
                            </div>
                        </div>
                    </div>

                    <!-- Action Buttons -->
                    <div class="form-actions">
                        <a id="cancelBtn" class="btn btn-cancel-custom d-none" href="#"><ui:text key="screen.19766ed6ccb2f4a3"/></a>
                        <button id="editBtn" type="button" class="btn btn-save-custom"><ui:text key="screen.fec2ac0f4cf167e3"/></button>
                        <button id="saveBtn" type="submit" class="btn btn-save-custom d-none"><ui:text key="screen.1509f561f2416598"/></button>
                    </div>
                </form>
            </div>
        </div>

        <!-- Include common site footer -->
        <%@include file="/WEB-INF/include/footer.jsp" %>

        <!-- Bootstrap JavaScript -->
        <script src="<%= request.getContextPath()%>/assets/js/bootstrap.bundle.min.js"></script>

        <!-- Toggle Edit Mode Script -->
        <script type="text/javascript">
            window.addEventListener('DOMContentLoaded', function () {
                const inputElements = document.querySelectorAll(".input_type");
                const cancelBtn = document.getElementById("cancelBtn");
                const editBtn = document.getElementById("editBtn");
                const saveBtn = document.getElementById("saveBtn");

                // Enable inputs when 'Edit Profile' is clicked
                editBtn.addEventListener("click", function () {
                    inputElements.forEach(x => {
                        x.readOnly = false;
                        x.classList.add("default_input");

                        if (x.tagName === "SELECT") {
                            x.disabled = false;
                        }
                    });

                    cancelBtn.classList.remove("d-none");
                    saveBtn.classList.remove("d-none");
                    editBtn.classList.add("d-none");
                });

                // Reload page on cancel
                cancelBtn.addEventListener("click", function (e) {
                    e.preventDefault();
                    location.reload();
                });
            });
        </script>

        <!-- Auto-submit avatar on file change -->

        <script>
            document.getElementById("avatarFile")
                    .addEventListener("change", function () {

                        const file = this.files[0];
                        const errorBox = document.getElementById("avatarError");

                        function showError(message) {
                            errorBox.textContent = message;
                            errorBox.classList.remove("d-none");
                        }

                        errorBox.textContent = "";
                        errorBox.classList.add("d-none");

                        if (!file) {
                            return;
                        }

                        const maxSize = 5 * 1024 * 1024;

                        const allowedTypes = [
                            "image/jpeg",
                            "image/png"
                        ];

                        if (!allowedTypes.includes(file.type)) {
                            showError("Only JPG and PNG images are allowed.");
                            this.value = "";
                            return;
                        }

                        if (file.size > maxSize) {
                            showError("Image size must not exceed 5 MB.");
                            this.value = "";
                            return;
                        }

                        if (file.size === 0) {
                            showError("Please select a non-empty image.");
                            this.value = "";
                            return;
                        }

                        // Valid image: submit to existing AvatarServlet.
                        this.form.submit();
                    });
        </script>


        <!-- Dynamic Country/City Dropdown -->
        <script>
            document.addEventListener("DOMContentLoaded", function () {
                const country = document.getElementById("country");
                const city = document.getElementById("city");
                const error = document.getElementById("locationError");
                const form = document.getElementById("form-1");
                const saveBtn = document.getElementById("saveBtn");

                const savedCountry = country.dataset.savedCountry || "";
                const savedCity = city.dataset.savedCity || "";

                let cityReady = true;
                let countryRequest = 0;

                function showError(message) {
                    error.textContent = message;
                    error.classList.toggle("d-none", !message);
                }

                function setOptions(select, values, selected, placeholder) {
                    select.innerHTML = "";

                    const defaultOption = new Option(
                            placeholder, "", false, !selected
                            );
                    select.add(defaultOption);

                    // Preserve the saved value even if it is absent
                    // from the remote API response.
                    const items = [...new Set(
                                [...values, ...(selected ? [selected] : [])]
                                )];

                    items.forEach(value => {
                        select.add(new Option(value, value));
                    });

                    select.value = selected || "";
                }


                async function loadCountries() {
                    const endpoints = [
                        "https://countriesnow.space/api/v0.1/countries/positions",
                        "https://countriesnow.space/api/v0.1/countries"
                    ];

                    for (const url of endpoints) {
                        try {
                            const response = await fetch(url);
                            if (!response.ok) {
                                throw new Error("HTTP " + response.status);
                            }

                            const data = await response.json();

                            if (data.error || !Array.isArray(data.data)) {
                                throw new Error("Invalid country data");
                            }

                            const names = data.data
                                    .map(item => item.name)
                                    .filter(Boolean)
                                    .sort((a, b) => a.localeCompare(b));

                            if (names.length === 0) {
                                throw new Error("Empty country list");
                            }

                            setOptions(
                                    country,
                                    names,
                                    savedCountry,
                                    "-- Select Country --"
                                    );

                            showError("");
                            return;

                        } catch (e) {
                            console.warn(
                                    "Failed to load countries:",
                                    url,
                                    e
                                    );
                        }
                    }

                    // Both API requests failed.
                    // Preserve the existing country.
                    setOptions(
                            country,
                            [],
                            savedCountry,
                            "-- Select Country --"
                            );

                    showError(
                            "Cannot load country list. " +
                            "Please check your connection and try again."
                            );
                }


                async function loadCities(selectedCountry, initialLoad = false) {
                    const requestId = ++countryRequest;

                    if (!selectedCountry) {
                        setOptions(city, [], "", "-- Select City --");
                        cityReady = true;
                        return;
                    }

                    cityReady = false;

                    if (!initialLoad) {
                        setOptions(city, [], "", "Loading cities...");
                    }

                    try {
                        const response = await fetch(
                                "https://countriesnow.space/api/v0.1/countries/cities",
                                {
                                    method: "POST",
                                    headers: {
                                        "Content-Type": "application/json"
                                    },
                                    body: JSON.stringify({
                                        country: selectedCountry
                                    })
                                }
                        );

                        if (!response.ok) {
                            throw new Error("City API unavailable");
                        }

                        const data = await response.json();

                        if (!Array.isArray(data.data)) {
                            throw new Error("Invalid city response");
                        }

                        // Ignore an outdated response.
                        if (requestId !== countryRequest) {
                            return;
                        }

                        const selected = initialLoad &&
                                selectedCountry === savedCountry
                                ? savedCity : "";

                        setOptions(
                                city,
                                data.data,
                                selected,
                                "-- Select City --"
                                );

                        cityReady = true;
                        showError("");

                    } catch (e) {
                        if (requestId !== countryRequest) {
                            return;
                        }

                        if (selectedCountry === savedCountry) {
                            setOptions(
                                    city, [], savedCity,
                                    "-- Select City --"
                                    );
                            cityReady = true;

                            showError(
                                    "City list is unavailable. " +
                                    "Your saved city is preserved."
                                    );
                        } else {
                            setOptions(
                                    city, [], "",
                                    "-- Select City --"
                                    );

                            cityReady = false;

                            showError(
                                    "Cannot load cities for this country. " +
                                    "Please try again."
                                    );
                        }
                }
                }

                country.addEventListener("change", function () {
                    showError("");
                    loadCities(country.value);
                });

                form.addEventListener("submit", function (event) {
                    if (!cityReady ||
                            (country.value && !city.value)) {

                        event.preventDefault();

                        showError(
                                "Please select a valid city before saving."
                                );

                        return;
                    }

                    // Avoid accidental double submission.
                    saveBtn.disabled = true;
                });

                // Restore the saved state before API calls.
                setOptions(
                        country, [], savedCountry,
                        "-- Select Country --"
                        );

                setOptions(
                        city, [], savedCity,
                        "-- Select City --"
                        );

                loadCountries();

                if (savedCountry) {
                    loadCities(savedCountry, true);
                }
            });
        </script>
    </body>
</html>