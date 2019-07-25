/**
 * HTTPAPI: check_me
 * 
 * @param site{String} - Site ID
 *   - `op37gt0s6mishr7rm4pav44dp9`
 * @param ticket{String} - Ticket for session
 *   - `op37gt0s6mishr7rm4pav44dp9/0scnfqjhpehkuouclico8se9sc`
 * 
 */
//---------------------------------------------
// Input Params
var __input = sys.json(params) || "{}";
var __inmap = JSON.parse(__input);
//---------------------------------------------
// Main Function
//---------------------------------------------
function __main__(params) {
  var me = null
  var ticket = null

  // No Login
  if(!me || !ticket) {
    sys.exec("ajaxre -cqn -e 'e.www.api.auth.nologin'")
  }
  // Output result
  else {
    var json = JSON.stringify({
      me : me,
      ticket : ticket
    })
    sys.exec("ajaxre -cqn", json)
  }
}


//---------------------------------------------
// Entry
//---------------------------------------------
__main__(__inmap)