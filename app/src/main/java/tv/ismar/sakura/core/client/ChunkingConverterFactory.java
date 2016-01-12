package tv.ismar.sakura.core.client;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.http.Body;

import static retrofit2.Converter.Factory;

/**
 * Created by huaijie on 1/12/16.
 */
public class ChunkingConverterFactory extends Factory {
    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] annotations,
                                                          Retrofit retrofit) {
        boolean isBody = false;
        boolean isChunked = false;
        for (Annotation annotation : annotations) {
            isBody |= annotation instanceof Body;
            isChunked |= annotation instanceof Chunked;
        }
        if (!isBody || !isChunked) {
            return null;
        }

        // Look up the real converter to delegate to.
        final Converter<Object, RequestBody> delegate =
                retrofit.nextRequestBodyConverter(this, type, annotations);
        // Wrap it in a Converter which removes the content length from the delegate's body.
        return new Converter<Object, RequestBody>() {
            @Override
            public RequestBody convert(Object value) throws IOException {
                final RequestBody realBody = delegate.convert(value);
                return new RequestBody() {
                    @Override
                    public MediaType contentType() {
                        return realBody.contentType();
                    }

                    @Override
                    public void writeTo(BufferedSink sink) throws IOException {
                        realBody.writeTo(sink);
                    }
                };
            }
        };
    }

}
