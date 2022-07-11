package com.nulabinc.backlog4j.api.option;

import com.nulabinc.backlog4j.http.BacklogHttpClientImpl;
import org.junit.jupiter.api.Test;

/**
 * @author nulab-inc
 */
public class QueryParamsTest {

    @Test
    public void minIdTest() {
        // when
        QueryParams params = new QueryParams();
        params.minId(123);

        // then
        String query = new BacklogHttpClientImpl().getParamsString(true, params);
        assert query.contains("&minId=123");
    }

    @Test
    public void maxIdTest() {
        // when
        QueryParams params = new QueryParams();
        params.maxId(999);

        // then
        String query = new BacklogHttpClientImpl().getParamsString(true, params);
        assert query.contains("&maxId=999");
    }

    @Test
    public void orderTest() {
        // when
        QueryParams params = new QueryParams();
        params.order(QueryParams.Order.Desc);

        // then
        String query = new BacklogHttpClientImpl().getParamsString(true, params);
        assert query.contains("&order=desc");

        // when
        params = new QueryParams();
        params.order(QueryParams.Order.Asc);

        // then
        query = new BacklogHttpClientImpl().getParamsString(false, params);
        assert query.contains("?order=asc");

    }

    @Test
    public void countTest() {
        // when
        QueryParams params = new QueryParams();
        params.count(222);

        // then
        String query = new BacklogHttpClientImpl().getParamsString(false, params);
        assert query.contains("?count=222");
    }
}
