/**
 * HTTPAPI: login_by_wxcode
 * 
 * @param site{String} - Site ID
 *   - `op37gt0s6mishr7rm4pav44dp9`
 * @param code{String} - Weixin Auth Code
 *   - `xxx`
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