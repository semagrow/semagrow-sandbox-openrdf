package eu.semagrow.sandbox.openrdf.api.impl;

import eu.semagrow.sandbox.openrdf.api.OpenRDFDataService;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.query.resultio.BooleanQueryResultWriterRegistry;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.sail.Sail;

/**
 *
 * @author http://www.turnguard.com/turnguard
 */
public class OpenRDFDataServiceImpl implements OpenRDFDataService {
    
    private Sail sail;
    private Repository repository;        

    public OpenRDFDataServiceImpl(Sail sail) throws RepositoryException {
        this.sail = sail;
        this.repository = new SailRepository(sail);
        this.repository.initialize();
    }
    
    public void shutDown(){
        try {
            this.repository.shutDown();
        } catch (RepositoryException ex) {}
    }
    
    /* QUERYING SECTION START */
    
    public void doQuery(String sparqlQuery, String acceptMimeType, OutputStream out) 
            throws RepositoryException, 
                   MalformedQueryException, 
                   QueryEvaluationException, 
                   TupleQueryResultHandlerException, 
                   RDFHandlerException,
                   IOException {
        
        RepositoryConnection repCon = null;
        Query query = null;
        try {            
            repCon = this.repository.getConnection();                        
            query = repCon.prepareQuery(QueryLanguage.SPARQL, sparqlQuery);            
            if(query instanceof TupleQuery){
                this.doSelectQuery((TupleQuery)query, acceptMimeType, out);
            }
            if(query instanceof GraphQuery){
                this.doConstructQuery((GraphQuery)query, acceptMimeType, out);
            }
            if(query instanceof BooleanQuery){
                this.doAskQuery((BooleanQuery)query, acceptMimeType, out);
            }            
        } finally {
            if(repCon!=null){
                try {
                    repCon.close();
                } catch (RepositoryException ex) {}
            }
        }
    }

    private void doSelectQuery(TupleQuery selectQuery, String acceptMimeType, OutputStream out) throws RepositoryException, MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException {        
        TupleQueryResultFormat format = TupleQueryResultFormat.forMIMEType(acceptMimeType, TupleQueryResultFormat.SPARQL);
        selectQuery.evaluate(QueryResultIO.createWriter(format, out));
    }
    
    private void doConstructQuery(GraphQuery constructQuery, String acceptMimeType, OutputStream out) throws RepositoryException, MalformedQueryException, QueryEvaluationException, RDFHandlerException{
        RDFFormat format = RDFFormat.forMIMEType(acceptMimeType, RDFFormat.TURTLE);
        constructQuery.evaluate(Rio.createWriter(format, out));
    }
    
    private void doAskQuery(BooleanQuery askQuery, String acceptMimeType, OutputStream out) throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException{
        BooleanQueryResultFormat format = BooleanQueryResultFormat.forMIMEType(acceptMimeType, BooleanQueryResultFormat.TEXT);
        BooleanQueryResultWriter writer = BooleanQueryResultWriterRegistry.getInstance().get(format).getWriter(out);
        writer.write(askQuery.evaluate());        
    }     
    
    /* QUERYING SECTION END */
    
    /* IMPORT SECTION START */
    public void importRDF(String rdfString, String baseURL, String mimeType) throws RepositoryException, IOException, RDFParseException, RDFHandlerException {
        RepositoryConnection repCon = null;
        RDFFormat format;
        RDFParser parser;
        RDFHandler handler;
        try {            
            repCon = this.repository.getConnection(); 
            repCon.setAutoCommit(false);
            format = RDFFormat.forMIMEType(mimeType);
            parser = Rio.createParser(format);
            handler = new RDFInserter(repCon);
            parser.setRDFHandler(handler);
            parser.parse(new StringReader(rdfString), baseURL);
            repCon.commit();
        } finally {
            if(repCon!=null){
                try {
                    repCon.close();
                } catch (RepositoryException ex) {}
            }
        }        
    }
    /* IMPORT SECTION END */
    
    /* TRIPLE IO SECTION START */
    
    public void addTriple(URI s, URI p, Value o) throws RepositoryException {
        RepositoryConnection repCon = null;        
        try {            
            repCon = this.repository.getConnection(); 
            repCon.setAutoCommit(false);
            repCon.add(s, p, o);
            repCon.commit();
        } finally {
            if(repCon!=null){
                try {
                    repCon.close();
                } catch (RepositoryException ex) {}
            }
        }         
    }
    
    public void removeTriple(URI s, URI p, Value o) throws RepositoryException{
        RepositoryConnection repCon = null;        
        try {            
            repCon = this.repository.getConnection(); 
            repCon.setAutoCommit(false);
            repCon.remove(s, p, o);
            repCon.commit();
        } finally {
            if(repCon!=null){
                try {
                    repCon.close();
                } catch (RepositoryException ex) {}
            }
        }         
    }    
    
    /* TRIPLE IO SECTION END */        

    public String explain(String sparqlQuery) throws RepositoryException, MalformedQueryException {
        RepositoryConnection repCon = null;        
        Query q = null;
        try {            
            repCon = this.repository.getConnection(); 
            q = repCon.prepareQuery(QueryLanguage.SPARQL, sparqlQuery);
            return q.toString();
        } finally {
            if(repCon!=null){
                try {
                    repCon.close();
                } catch (RepositoryException ex) {}
            }
        }        
    }
}
