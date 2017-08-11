/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scf;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author michael.theuerzeit
 */
public class Import extends HttpServlet {
    
    private String dirName;

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {          
        try { 
            JSONObject json = this.getJsonFromRequest(request);            
            this.importFiles(json);
            String location = this.getRedirectUrl(json);            
            response.sendRedirect(location);  
//            response.setContentType("text/plain;charset=UTF-8");
//            try (PrintWriter out = response.getWriter()) {                
//                out.println(location);
//            }            
        } catch(IOException | ParseException e) {
            response.setStatus(400);
            response.setContentType("text/plain;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {                
                out.println(e.getMessage());
            }
        }
    }
    
    private JSONObject getJsonFromRequest(HttpServletRequest request) throws IOException, ParseException {
        StringBuilder sb = new StringBuilder(); 
        JSONParser parser = new JSONParser();
        
        BufferedReader reader = request.getReader(); 
        
        String line;            
        while( (line = reader.readLine()) != null ) {
            sb.append(line).append("\n");
        }
        
        Object obj = parser.parse(sb.toString());
        JSONObject json = (JSONObject) obj;
        
        return json;
    }
    
    private void importFiles(JSONObject json) throws FileNotFoundException, IOException {        
        JSONArray files = (JSONArray) json.get("files");
        Iterator<JSONObject> fileIterator = files.iterator();
        while (fileIterator.hasNext()) {
            JSONObject item = fileIterator.next();

            String content = (String) item.get("content");
            String filename = this.getDirName() + File.separatorChar + (String) item.get("name");                

            File file = new File(filename);
            BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(file));
            writer.write( DatatypeConverter.parseBase64Binary(content) );
            writer.flush();
        }        
    }
    
    private String getRedirectUrl(JSONObject json) throws FileNotFoundException, IOException {          
        String location = "/birt/preview?__report=";
        location += (String) json.get("report");                        
        JSONArray params = (JSONArray) json.get("params");
        Iterator<JSONObject> paramsIterator = params.iterator();
        while (paramsIterator.hasNext()) {
            JSONObject param = paramsIterator.next();
            for(Iterator iterator = param.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();                    
                location += "&"+key+"="+param.get(key);
            }
        }
        return location;        
    }
    
    private String getDirName() {
        if( this.dirName == null ) {            
            String currentPath = getServletContext().getRealPath("/");
            String basePath = currentPath;             
            this.dirName = basePath + "assets";
            (new File(dirName)).mkdirs();            
        }
        return this.dirName;
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
