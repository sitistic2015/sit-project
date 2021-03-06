package dao;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.error.FlushDisabledException;
import com.couchbase.client.java.view.*;
import entity.AbstractEntity;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import util.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for DAO
 * AbstractDAO provides methods for DAO
 * Also use for connect and disconnect
 */
public abstract class AbstractDAO<T extends AbstractEntity> {
    /**
     * Current Connection
     */
    protected Cluster currentCluster;

    /**
     * Current Bucket
     */
    protected Bucket currentBucket;

    /**
     * datatype of T
     */
    protected String datatype;

    /**
     * Connect to BDD and
     * @return Bucket to communicate with couchbase
     */
    public final void connect() {
        if(currentCluster == null || currentBucket==null) {
            // Connect to a cluster
            currentCluster = CouchbaseCluster.create(Configuration.COUCHBASE_HOSTNAME);

            // Open a bucket
            currentBucket = currentCluster.openBucket(Configuration.BUCKET_NAME);
        }
    }

    /**
     * Disconnect BDD
     */
    public final void disconnect() {
        if(currentCluster != null)
        {
            currentCluster.disconnect();
            currentBucket =null;
        }
    }

    /**
     * Create an entity
     * @param e entity to create
     */
    public final T create(T e) {
        JsonDocument res = currentBucket.insert(entityToJsonDocument(e));
        return jsonDocumentToEntity(res);
    }

    /**
     * Delete an entity
     * @param e
     */
    public final T delete(T e) {
        JsonDocument res = currentBucket.remove(""+e.getId());
        return jsonDocumentToEntity(res);
    }

    /**
     * Update entity
     * @param e
     */
    public final T update(T e) {
        JsonDocument res = currentBucket.upsert(entityToJsonDocument(e));
        return jsonDocumentToEntity(res);
    }

    /**
     * GetAll
     * @return
     */
    public final List<T> getAll()
    {
        List<T> res = new ArrayList<T>();
        DesignDocument designDoc = currentBucket.bucketManager().getDesignDocument("designDoc");
        createViewAll();
        ViewResult result = currentBucket.query(ViewQuery.from("designDoc", "by_datatype_" + datatype));
                // Iterate through the returned ViewRows
        for (ViewRow row : result) {
            System.out.println(row);
            res.add(jsonDocumentToEntity(row.document()));
        }
        return res;
    }

    /**
     * GetById
     * @return
     */
    public final T getById(long id)
    {
        JsonDocument res = currentBucket.get(""+id);
        return jsonDocumentToEntity(res);
    }

    /**
     * flush our bucket
     * @return
     */
    public boolean flush()
    {
        if(currentBucket!=null && currentCluster!=null)
        {
            try
            {
                return currentBucket.bucketManager().flush();
            }
            catch (FlushDisabledException e)
            {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * Transform a jsonDocument to entity
     * @param jsonDocument document to transform
     * @return entity of JsonDocument
     */
    protected abstract T jsonDocumentToEntity(JsonDocument jsonDocument);

    /**
     * Transform an entity to JsonDocument
     * @param entity to transform
     * @return jsonDoc of entity
     */
    protected abstract JsonDocument entityToJsonDocument(T entity);

    private void createViewAll()
    {
        DesignDocument designDoc = currentBucket.bucketManager().getDesignDocument("designDoc");

            String viewName = "by_datatype_"+datatype;
            String mapFunction =
                    "function (doc, meta) {\n" +
                            " if(doc.properties.datatype && doc.properties.datatype == '"+ datatype + "') \n" +
                            "   { emit(doc.firstname);}\n" +
                            "}";
            designDoc.views().add(DefaultView.create(viewName, mapFunction, ""));
            currentBucket.bucketManager().upsertDesignDocument(designDoc);
    }

    public void createDesignDocument()
    {
            List<View> views = new ArrayList<View>();
            DesignDocument designDoc = DesignDocument.create("designDoc", views);
            currentBucket.bucketManager().insertDesignDocument(designDoc);
    }
}
