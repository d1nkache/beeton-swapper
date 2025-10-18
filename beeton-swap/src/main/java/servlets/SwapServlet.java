package servlets;

import java.io.PrintWriter;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/swap/desust")
public class SwapServlet extends HttpServlet {
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jettonA = request.getParameter("jettonA");
        String jettonB = request.getParameter("jettonB");

        PrintWriter out = response.getWriter();
        String json = "{\"send\": 100, \"get\": 100, \"commission\": 100, \"status\": \"success\"}";
        out.println(json);
    }
}