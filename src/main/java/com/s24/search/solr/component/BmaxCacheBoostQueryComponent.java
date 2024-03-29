package com.s24.search.solr.component;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.ProductFloatFunction;
import org.apache.lucene.queries.function.valuesource.QueryValueSource;
import org.apache.lucene.search.Query;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.search.QParser;

import com.s24.search.solr.functions.FloatCachingValueSource;

/**
 * Replaces given boost queries with a single cached boost function.
 * 
 * @author Shopping24 GmbH, Torsten Bøgh Köster (@tboeghk)
 */
public class BmaxCacheBoostQueryComponent extends AbstractCachingComponent {

   public BmaxCacheBoostQueryComponent() {
      super("bq", "bf");
   }

   @Override
   protected ValueSource wrapInCachingValueSource(ValueSource function, int maxDocs) {
      return new FloatCachingValueSource(function, maxDocs, FloatCachingValueSource.CACHE_SPARSE);
   }

   /**
    * {@inheritDoc}
    */
   protected ValueSource compileValueFunctions(ResponseBuilder rb, String[] boosts)
         throws Exception {
      checkNotNull(rb, "Pre-condition violated: rb must not be null.");
      checkNotNull(boosts, "Pre-condition violated: boosts must not be null.");

      ValueSource[] functions = new ValueSource[boosts.length];

      // iterate boost params
      for (int i = 0; i < boosts.length; i++) {
         Query q = QParser.getParser(boosts[i], null, rb.req).getQuery();
         functions[i] = new QueryValueSource(q, 0f);
      }

      // do not encapsulate unnecessary
      if (functions.length == 1) {
         return functions[0];
      }

      // compute a product of all value sources function.
      return new ProductFloatFunction(functions);
   }

   @Override
   public String getDescription() {
      return "Replaces configured boost queries with a single cached function.";
   }

}
