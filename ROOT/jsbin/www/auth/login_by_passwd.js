/**
 * HTTPAPI: login_by_passwd
 * 
 * @param site{String} - Site ID
 *   - `op37gt0s6mishr7rm4pav44dp9`
 * @param phone{String} - Phone to login
 *   - `139..`
 * @param name{String} - Name to login
 *   - `xiaobai`
 * @param passwd{String} - Password to verify
 *   - `123456`
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