/**
 *
 *  SmartThings bridge for socket-control <https://github.com/drweaver/socket-control>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 
import groovy.json.JsonSlurper
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


preferences {
        input("ip", "string", title:"IP Address", description: "192.168.0.55", defaultValue: "192.168.0.55" ,required: true, displayDuringSetup: true)
        input("port", "string", title:"Port", description: "3003", defaultValue: "3003" , required: true, displayDuringSetup: true)
        input("socketId", "string", title:"Socket Id", description: "Socket Id", defaultValue: "1", required:true, displayDuringSetup: true)
}

metadata {
	definition (name: "Socket-Control Swith", namespace: "shaneweaver", author: "Shane Weaver") {
        capability "Switch"        
	}

	tiles(scale: 2) {
        
        standardTile("switch", "device.switch", width: 4, height: 4) {
			state "on", label:"Front 1", action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
			state "off", label:"Front 1", action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}

        main "switch"
        details("switch")
    }
}

// ------------------------------------------------------------------

// parse events into attributes
def parse(String description) {
    log.debug "description: $description"
    
	try {
        def map = [:]
        def descMap = parseDescriptionAsMap(description)
        log.debug "descMap: ${descMap}"
        
        def body = new String(descMap["body"].decodeBase64())
        log.debug "body: ${body}"
        
        def slurper = new JsonSlurper()
        def result = slurper.parseText(body)
        
        log.debug "result: ${result}"

        if( result.containsKey("status") ) {
            log.debug "creating event for status: ${result.status}"
            return createEvent(name: "switch", value: result.status)
        }
    } catch( Exception ex) {
        log.debug "exception in parse"
    }
 	
}

def on() {
    postAction("on");
}

def off() {
    postAction("off");
}

def installed() {
	log.debug "Executing 'installed'"
	updated();
}

def updated() {
	log.debug "Executing 'updated'"
}




// ------------------------------------------------------------------

private postAction(action){
  setDeviceNetworkId(ip,port)  
  
  def hubAction = new physicalgraph.device.HubAction(
    method: "POST",
    // path: uri,
    headers: getHeader(),
    body: ["socket": doorId,"action": action]
  )//,delayAction(1000), refresh()]
  log.debug("Executing hubAction on " + getHostAddress())
  //log.debug hubAction
  sendHubCommand(hubAction)
  //hubAction    
}

// ------------------------------------------------------------------
// Helper methods
// ------------------------------------------------------------------

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}


def toAscii(s){
        StringBuilder sb = new StringBuilder();
        String ascString = null;
        long asciiInt;
                for (int i = 0; i < s.length(); i++){
                    sb.append((int)s.charAt(i));
                    sb.append("|");
                    char c = s.charAt(i);
                }
                ascString = sb.toString();
                asciiInt = Long.parseLong(ascString);
                return asciiInt;
    }

private encodeCredentials(username, password){
	log.debug "Encoding credentials"
	def userpassascii = "${username}:${password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    //log.debug "ASCII credentials are ${userpassascii}"
    //log.debug "Credentials are ${userpass}"
    return userpass
}

private getHeader(){
	log.debug "Getting headers"
    def headers = [:]
    headers.put("HOST", getHostAddress())
    log.debug "Headers are ${headers}"
    return headers
}

private delayAction(long time) {
	new physicalgraph.device.HubAction("delay $time")
}

private setDeviceNetworkId(ip,port){
  	def iphex = convertIPtoHex(ip).toUpperCase()
  	def porthex = convertPortToHex(port).toUpperCase()
  	device.deviceNetworkId = "$iphex:$porthex".toUpperCase()
  	log.debug "Device Network Id set to ${device.deviceNetworkId}"
}

private getHostAddress() {
	return "${ip}:${port}"
}

// gets the address of the Hub
private getCallBackAddress() {
    return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}