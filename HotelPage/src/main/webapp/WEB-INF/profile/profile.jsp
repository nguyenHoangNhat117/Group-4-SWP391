<%-- 
    Document   : revenue_chart
    Created on : Jul 11, 2025, 1:34:58 PM
    Author     : Trần Nguyễn
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="model.Customer" %>
<%
    // Get customer object from request (used to render user profile)
    Customer customer = (Customer) request.getAttribute("customer");
    if (customer == null) {
        customer = new Customer(); // fallback if null to avoid null pointer exceptions
    }
%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Profile Page</title>

        <!-- Bootstrap & Custom CSS -->
        <link href="<%= request.getContextPath()%>/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="<%= request.getContextPath()%>/assets/css/general.css" rel="stylesheet">
        <link rel="stylesheet" href="<%= request.getContextPath()%>/assets/css/profile.css">

        <!-- FontAwesome Icons -->
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    </head>

    <body>
        <!-- Include common site header -->
        <%@include file="/WEB-INF/include/header.jsp" %>

        <div class="profile-container">
            <!-- Left Sidebar for Avatar and Basic Info -->
            <div class="profile-sidebar glass-card">
                <form action="<%= request.getContextPath()%>/avatar" method="post" enctype="multipart/form-data">
                    <!-- Display avatar or default image -->
                         <img src="<%= request.getContextPath()%>/assets/img/<%=(customer.getAvatar() != null && !customer.getAvatar().isEmpty()) ? customer.getAvatar() + "?v=" + System.currentTimeMillis() : "avatar.jpg"%>"
                         alt="avatar" class="img-thumbnail" width="150" loading="lazy">

                    <!-- Upload avatar trigger -->
                    <label for="avatarFile" class="upload-label">
                        <i class="fas fa-camera upload-icon"></i>
                        <span>Upload your photo</span>
                    </label>
                    <input type="file" id="avatarFile" name="img" accept=".png, .jpg, .jpeg" class="d-none">
                </form>

                <!-- Display customer name and email -->
                <h4><%= customer.getFirstName() != null ? customer.getFirstName() : ""%> <%= customer.getLastName() != null ? customer.getLastName() : ""%></h4>
                <p><%= customer.getEmail() != null ? customer.getEmail() : ""%></p>

                <div class="account-section">
                    <button class="account-btn active">Account Details</button>
                </div>
            </div>

            <!-- Main Profile Form Area -->
            <div class="profile-main glass-card">
                <form id="form-1" method="post" action="<%= request.getContextPath()%>/profile">
                    <!-- Personal Details Section -->
                    <div class="form-section">
                        <h5>Personal Details</h5>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label">Username</label>
                                <input type="text" class="form-control" name="username" value="${loggedUser.username}" readonly>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Display Name</label>
                                <input type="text" class="form-control" name="display-name" value="${loggedUser.displayName}">
                            </div>
                        </div>

                        <div class="row g-3 mt-3">
                            <div class="col-md-6">
                                <label class="form-label">First Name</label>
                                <input type="text" class="form-control input_type" name="firstName" value="<%= customer.getFirstName() != null ? customer.getFirstName() : ""%>" readonly>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Last Name</label>
                                <input type="text" class="form-control input_type" name="lastName" value="<%= customer.getLastName() != null ? customer.getLastName() : ""%>" readonly>
                            </div>
                        </div>

                        <div class="row g-3 mt-3">
                            <div class="col-md-6">
                                <label class="form-label">Email</label>
                                <input type="email" class="form-control input_type" name="email" value="<%= customer.getEmail() != null ? customer.getEmail() : ""%>" readonly>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Phone</label>
                                <input type="text" class="form-control input_type" name="phone" value="<%= customer.getPhone() != null ? customer.getPhone() : ""%>" readonly>
                            </div>
                        </div>
                    </div>

                    <!-- Address Section -->
                    <div class="form-section">
                        <h5>Address</h5>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label">Country</label>
                                <select id="country" name="country" class="form-control input_type" disabled></select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">City</label>
                                <select id="city" name="city" class="form-control input_type" disabled></select>
                            </div>
                        </div>

                        <div class="row g-3 mt-3">
                            <div class="col-12">
                                <label class="form-label">Street</label>
                                <input type="text" class="form-control input_type" name="street" value="<%= customer.getStreet() != null ? customer.getStreet() : ""%>" readonly>
                            </div>
                        </div>
                    </div>

                    <!-- Action Buttons -->
                    <div class="form-actions">
                        <a id="cancelBtn" class="btn btn-cancel-custom d-none" href="#">Cancel</a>
                        <button id="editBtn" type="button" class="btn btn-save-custom">Edit Profile</button>
                        <button id="saveBtn" type="submit" class="btn btn-save-custom d-none">Save</button>
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
            document.getElementById('avatarFile').addEventListener('change', function () {
                this.form.submit();
            });
        </script>

        <!-- Dynamic Country/City Dropdown -->
        <script>
            // Populate countries
            fetch("https://countriesnow.space/api/v0.1/countries/positions")
                    .then(res => res.json())
                    .then(data => {
                        const countrySelect = document.getElementById("country");

                        // Default option
                        const defaultOption = document.createElement("option");
                        defaultOption.value = "";
                        defaultOption.text = "-- Select Country --";
                        defaultOption.disabled = true;
                        defaultOption.selected = true;
                        countrySelect.appendChild(defaultOption);

                        // Add countries
                        data.data.forEach(country => {
                            const option = document.createElement("option");
                            option.value = country.name;
                            option.text = country.name;
                            countrySelect.appendChild(option);
                        });

                        // Select current user country if available
                        const currentCountry = "<%= customer.getCountry() != null ? customer.getCountry() : ""%>";
                        if (currentCountry !== "") {
                            countrySelect.value = currentCountry;
                            countrySelect.dispatchEvent(new Event("change"));
                        }
                    });

            // Populate cities based on selected country
            document.getElementById("country").addEventListener("change", function () {
                const selectedCountry = this.value;
                fetch("https://countriesnow.space/api/v0.1/countries/cities", {
                    method: "POST",
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({country: selectedCountry})
                })
                        .then(res => res.json())
                        .then(data => {
                            const citySelect = document.getElementById("city");
                            citySelect.innerHTML = "";

                            // Default city option
                            const defaultCityOption = document.createElement("option");
                            defaultCityOption.value = "";
                            defaultCityOption.text = "-- Select City --";
                            defaultCityOption.disabled = true;
                            defaultCityOption.selected = true;
                            citySelect.appendChild(defaultCityOption);

                            // Add cities
                            data.data.forEach(city => {
                                const option = document.createElement("option");
                                option.value = city;
                                option.text = city;
                                citySelect.appendChild(option);
                            });

                            // Set selected city if exists
                            const currentCity = "<%= customer.getCity() != null ? customer.getCity() : ""%>";
                            if (currentCity !== "") {
                                citySelect.value = currentCity;
                            }
                        });
            });
        </script>
    </body>
</html>