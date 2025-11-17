package model;


public class SwapResponse {

    private String status;      
    private String direction;   
    private String route;  
    private String jettonA;   
    private String jettonB;  
    private String jettonAmount;
    private String message;   

    public SwapResponse() {}

    public SwapResponse(String status, String direction, String route,
                        String jettonA, String jettonB, String jettonAmount, String message) {
        this.status       = status;
        this.direction    = direction;
        this.route        = route;
        this.jettonA      = jettonA;
        this.jettonB      = jettonB;
        this.jettonAmount = jettonAmount;
        this.message      = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDirection() {
        return direction;
    }
    
    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getJettonA() {
        return jettonA;
    }

    public void setJettonA(String jettonA) {
        this.jettonA = jettonA;
    }

    public String getJettonB() {
        return jettonB;
    }

    public void setJettonB(String jettonB) {
        this.jettonB = jettonB;
    }

    public String getJettonAmount() {
        return jettonAmount;
    }

    public void setJettonAmount(String jettonAmount) {
        this.jettonAmount = jettonAmount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
