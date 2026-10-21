
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.DataProtection;
using System.IO;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddDataProtection()
    .SetApplicationName("BarakahTravel.SharedAuth")
    .PersistKeysToFileSystem(
        new DirectoryInfo(@"C:\BarakahTravelDevKeys"));


// API connection
builder.Services.AddHttpClient("BarakahAPI", client =>
{
    client.BaseAddress =
        new Uri("https://localhost:7000/");
});

// Razor Pages
builder.Services.AddRazorPages(options =>
{
    options.Conventions.AuthorizePage(
        "/CustomerDashboard", "CustomerOnly");
});

// Website cookie authentication
builder.Services
    .AddAuthentication(
        CookieAuthenticationDefaults.AuthenticationScheme)
    .AddCookie(options =>
    {
        options.LoginPath = "/Login";
        options.AccessDeniedPath = "/Login";

        options.Cookie.Name = "BarakahTravel.Web.Customer";
        options.Cookie.HttpOnly = true;
        options.Cookie.SecurePolicy =
            CookieSecurePolicy.Always;
        options.Cookie.SameSite =
            SameSiteMode.Lax;

        options.ExpireTimeSpan =
            TimeSpan.FromHours(2);

        options.SlidingExpiration = true;
    });

// Customer authorization policy
builder.Services.AddAuthorization(options =>
{
    options.AddPolicy("CustomerOnly", policy =>
    {
        policy.RequireAuthenticatedUser();
        policy.RequireRole("Customer");
    });
});

var app = builder.Build();

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Error");
    app.UseHsts();
}

app.UseHttpsRedirection();
app.UseStaticFiles();

app.UseRouting();

// Authentication must come before authorization.
app.UseAuthentication();
app.UseAuthorization();

app.MapRazorPages();

app.Run();
