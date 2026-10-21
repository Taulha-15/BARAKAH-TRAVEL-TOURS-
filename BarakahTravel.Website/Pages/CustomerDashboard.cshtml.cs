
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace BarakahTravel.Web.Pages;

[Authorize(Policy = "CustomerOnly")]
public class CustomerDashboardModel : PageModel
{
    public void OnGet()
    {
    }
}
