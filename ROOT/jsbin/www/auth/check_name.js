/**
 * HTTPAPI: check_name
 * 
 * @param site{String} - Site ID
 *   - `op37gt0s6mishr7rm4pav44dp9`
 * @param s{String} - The name to be checked
 *   - `xiaobai`
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
  var reo  = params  

  // Output result
  var json = JSON.stringify(reo)
  sys.exec("ajaxre -cqn", json)
}


//---------------------------------------------
// Entry
//---------------------------------------------
__main__(__inmap)