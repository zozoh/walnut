/**
 * HTTPAPI: get_vcode
 * 
 * @param site{String} - Site ID
 *   - `op37gt0s6mishr7rm4pav44dp9`
 * @param phone{String} - Phone to receive the verify code
 *   - `139..`
 * @param captcha{String} - Captcha to block robot
 *   - `4a5e`
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