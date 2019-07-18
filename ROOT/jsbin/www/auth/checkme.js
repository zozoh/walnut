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
  sys.out.println(JSON.stringify(params))
}


//---------------------------------------------
// Entry
//---------------------------------------------
__main__(__inmap)