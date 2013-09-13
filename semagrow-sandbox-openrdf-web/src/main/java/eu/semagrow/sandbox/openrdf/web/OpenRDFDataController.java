package eu.semagrow.sandbox.openrdf.web;

import eu.semagrow.sandbox.openrdf.api.OpenRDFDataService;
import eu.semagrow.sandbox.openrdf.api.impl.OpenRDFDataServiceImpl;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.Sail;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;
import org.openrdf.sail.rdbms.postgresql.PgSqlStore;

/**
 *
 * @author turnguard
 */
public class OpenRDFDataController extends HttpServlet {

    private OpenRDFDataService service;
    private Sail sail;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        /* CHOOSE STORE IMPLEMENTATION */
        
        sail = createSimpleMemorySail();
        //sail = createPostgresSail();
        //sail = createSimpleNativeSail();
        //sail = createPersistentMemorySail();
        
        /* additional sail impls do exist for nearly every tripleStore, 
         * ie.:
         * virtuoso, allegroGraph, mysql, 4Store, owlim, neo4j, lucene, ...         
         */
        try {
            service = new OpenRDFDataServiceImpl(sail);
        } catch (RepositoryException ex) {
            throw new ServletException(ex);
        }
    }
    

    @Override
    public void destroy() {
        super.destroy();
        this.service.shutDown();
    }
    
    /**
     * RUNNING SPARQL QUERIES ON HTTP-GET
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String sparqlQuery = request.getParameter("query");
        String acceptMimeType = request.getParameter("acceptMimeType");
        String explain = request.getParameter("explain");
        System.out.println("explain " + explain);
        if(sparqlQuery!= null && acceptMimeType!=null && explain==null){
                    try {
                        response.setContentType(acceptMimeType);
                        service.doQuery(sparqlQuery, acceptMimeType, response.getOutputStream());
                    } catch (RepositoryException ex) {
                        throw new ServletException(ex);
                    } catch (MalformedQueryException ex) {
                        throw new ServletException(ex);
                    } catch (QueryEvaluationException ex) {
                        throw new ServletException(ex);
                    } catch (TupleQueryResultHandlerException ex) {
                        throw new ServletException(ex);
                    } catch (RDFHandlerException ex) {
                        throw new ServletException(ex);
                    }
        } else
        if(sparqlQuery!=null && (explain!=null && explain.equals("on"))){
            request.setAttribute("query", sparqlQuery);
            try {
                request.setAttribute("explanation", service.explain(sparqlQuery));
                request.getRequestDispatcher("/sparql.jsp").forward(request, response);
            } catch (RepositoryException ex) {
                throw new ServletException(ex);
            } catch (MalformedQueryException ex) {
                throw new ServletException(ex);
            }
        }
    }
        
    /**
     * RUNNING IMPORTS, ADD/REMOVE ON HTTP-POSTS
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String rdf = req.getParameter("rdf");
        String mimeType = req.getParameter("mimeType");
        
        if(rdf!=null && mimeType!=null){
            try {
                service.importRDF(rdf, req.getRequestURL().toString(), mimeType);
                req.getRequestDispatcher("/import.jsp").forward(req, resp);
            } catch (RepositoryException ex) {
                throw new ServletException(ex);
            } catch (RDFParseException ex) {
                throw new ServletException(ex);
            } catch (RDFHandlerException ex) {
                throw new ServletException(ex);
            }
        } else {
            String s = req.getParameter("s");
            String p = req.getParameter("p");
            String o = req.getParameter("o");
            /* either add or remove */
            String action = req.getParameter("action");
            if(action.equals("add")){
                if(s!=null && p!=null && o!=null){
                    URI subject = new URIImpl(s);
                    URI predicate = new URIImpl(p);
                    Value object = null;
                    try {
                        object = new URIImpl(o);
                    } catch(Exception e){
                        object = new LiteralImpl(o);                        
                    }
                    try {
                        service.addTriple(subject, predicate, object);
                        req.getRequestDispatcher("/triple-io.jsp").forward(req, resp);
                    } catch (RepositoryException ex) {
                        throw new ServletException(ex);
                    }
                }
            }
            
            if(action.equals("remove")){                
                URI subject = s!=null?new URIImpl(s):null;
                URI predicate = p!=null?new URIImpl(p):null;
                Value object = null;
                try {
                    object = o!=null?new URIImpl(o):null;
                } catch(Exception e){
                    object = o!=null?new LiteralImpl(o):null;
                }
                try {
                    service.removeTriple(subject, predicate, object);
                    req.getRequestDispatcher("/triple-io.jsp").forward(req, resp);
                } catch (RepositoryException ex) {
                    throw new ServletException(ex);
                }
            }            
        }
        
        
        
    }
    
    
    
    /* SECTION FOR CREATING A SAIL-IMPL (USED IN init() above) */
    
    /**
     * Create a simple non-persisten memory store (data will be gone on shutdown)     
     */
    private Sail createSimpleMemorySail(){
        return new MemoryStore();
    }
    
    /**
     * Create a simple persistent memory store.
     * Replace dataDir with the absolute path of an actually writable directory     
     */
    private Sail createPersistentMemorySail(){
        MemoryStore memStore = new MemoryStore();
                    memStore.setPersist(true);
                    memStore.setSyncDelay(1000);
                    memStore.setDataDir(new File("/path/to/data/directory/memory/"));
        return memStore;
    }
    
    /**
     * Create a simple native store.
     * Replace dataDir with the absolute path of an actually writable directory     
     */    
    private Sail createSimpleNativeSail(){
        NativeStore nStore = new NativeStore();
                    nStore.setTripleIndexes("spoc,posc");
                    nStore.setDataDir(new File("/path/to/data/directory/native/"));
        return nStore;
    }
    
    /**
     * SEE HERE FOR SOME NOTES ON POSTGRES : http://www.openrdf.org/doc/sesame/users/ch02.html#d0e387
     * You'll need a postgres drive (jar) - best in tomcat's lib directory and an empty database,
     * along with user and password.
     * 
     * Replace values in the method below with actual values (SERVER_NAME, USER,...)
     * 
     */
    private Sail createPostgresSail(){
        PgSqlStore pgStore = new PgSqlStore();
                   pgStore.setServerName("SERVER_NAME");
                   pgStore.setUser("USER");
                   pgStore.setPassword("PASSWORD");
                   pgStore.setPortNumber(3636);
                   pgStore.setDatabaseName("DATABASE_NAME");                   
        return pgStore;
    }
}
