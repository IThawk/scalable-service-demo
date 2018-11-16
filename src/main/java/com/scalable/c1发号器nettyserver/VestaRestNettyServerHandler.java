package com.scalable.c1发号器nettyserver;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.AsciiString;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.cdel.util.helper.DateUtil;
import com.cdel.util.helper.JacksonUtil;
import com.scalable.c1发号器.bean.Id;
import com.scalable.c1发号器.service.IdService;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Sharable
public class VestaRestNettyServerHandler extends ChannelInboundHandlerAdapter {
    private static final String ID = "id";

    private static final String VERSION = "version";
    private static final String TYPE = "type";
    private static final String GENMETHOD = "genMethod";
    private static final String MACHINE = "machine";
    private static final String TIME = "time";
    private static final String SEQ = "seq";

    private static final String ACTION_GENID = "/genid";
    private static final String ACTION_EXPID = "/expid";
    private static final String ACTION_TRANSTIME = "/transtime";
    private static final String ACTION_MAKEID = "/makeid";
    private static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");
    private static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");
    private static final AsciiString CONNECTION = new AsciiString("Connection");
    private static final AsciiString KEEP_ALIVE = new AsciiString("keep-alive");

    private static final Log log = LogFactory.getLog(VestaRestNettyServerHandler.class);

    private IdService idServiceNetty;

    public VestaRestNettyServerHandler() {
        ApplicationContext ac = new ClassPathXmlApplicationContext("spring/vesta-service-sample.xml");
        idServiceNetty = (IdService) ac.getBean("idServiceNetty");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof HttpRequest))
            return;

        HttpRequest req = (HttpRequest) msg;
        String uri = req.uri();
        if (log.isDebugEnabled())
            log.debug("request uri==" + uri);

        long id = -1;
        long time = -1;
        long version = -1;
        long type = -1;
        long genmethod = -1;
        long machine = -1;
        long seq = -1;

        QueryStringDecoder decoderQuery = new QueryStringDecoder(uri);
        Map<String, List<String>> uriAttributes = decoderQuery.parameters();
        for (Entry<String, List<String>> attr : uriAttributes.entrySet()) {
            for (String attrVal : attr.getValue()) {
                if (log.isDebugEnabled())
                    log.debug("Request Parameter: " + attr.getKey() + '=' + attrVal);
                if (ID.equals(attr.getKey())) {
                    id = Long.parseLong(attrVal);
                } else if (TIME.equals(attr.getKey())) {
                    time = Long.parseLong(attrVal);
                } else if (VERSION.equals(attr.getKey())) {
                    version = Long.parseLong(attrVal);
                } else if (TYPE.equals(attr.getKey())) {
                    type = Long.parseLong(attrVal);
                } else if (GENMETHOD.equals(attr.getKey())) {
                    genmethod = Long.parseLong(attrVal);
                } else if (MACHINE.equals(attr.getKey())) {
                    machine = Long.parseLong(attrVal);
                } else if (SEQ.equals(attr.getKey())) {
                    seq = Long.parseLong(attrVal);
                }
            }
        }

        StringBuffer sbContent = new StringBuffer();

        if (uri.startsWith(ACTION_GENID)) {
            long idl = idServiceNetty.genId();

            if (log.isTraceEnabled())
                log.trace("Generated id: " + idl);

            sbContent.append(idl);
        } else if (uri.startsWith(ACTION_EXPID)) {
            Id ido = idServiceNetty.expId(id);

            if (log.isTraceEnabled())
                log.trace("Explained id: " + ido);

            sbContent.append(JacksonUtil.ObjecttoJSon(ido));
        } else if (uri.startsWith(ACTION_TRANSTIME)) {
            Date date = idServiceNetty.transTime(time);

            if (log.isTraceEnabled())
                log.trace("Time: " + date);

            sbContent.append(DateUtil.getStringDay(date));
        } else if (uri.startsWith(ACTION_MAKEID)) {
            long madeId = -1;

            if (time == -1 || seq == -1)
                sbContent.append("Both time and seq are required.");
            else if (version == -1) {
                if (type == -1) {
                    if (genmethod == -1) {
                        if (machine == -1) {
                            madeId = idServiceNetty.makeId(time, seq);
                        } else {
                            madeId = idServiceNetty.makeId(machine, time, seq);
                        }
                    } else {
                        madeId = idServiceNetty.makeId(genmethod, machine, time, seq);
                    }
                } else {
                    madeId = idServiceNetty.makeId(type, genmethod, machine, time, seq);
                }
            } else {
                madeId = idServiceNetty.makeId(version, type, genmethod, machine, time, seq);
            }


            if (log.isTraceEnabled())
                log.trace("Id: " + madeId);
            sbContent.append(madeId);
        } else {
            sbContent.append("\r\n");
            sbContent.append("Please input right URI:");
            sbContent.append("\r\n");
            sbContent.append("    Example 1: http://ip:port/genid");
            sbContent.append("\r\n");
            sbContent.append("    Example 2: http://ip:port/expid?id=?");
            sbContent.append("\r\n");
            sbContent.append("    Example 3: http://ip:port/transtime?time=?");
            sbContent.append("\r\n");
            sbContent.append("    Example 4: http://ip:port/makeid?version=?&type=?&genMethod=?&machine=?&time=?&seq=?");

        }

        if (log.isTraceEnabled())
            log.trace("Message body: " + sbContent);

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(sbContent.toString().getBytes(Charset.forName("UTF-8"))));

        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

        boolean keepAlive = HttpUtil.isKeepAlive(req);

        if (log.isTraceEnabled())
            log.trace("Keep Alive: " + keepAlive);

        if (!keepAlive) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, KEEP_ALIVE);
            ctx.write(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (log.isErrorEnabled())
            log.error("HTTP Server Error: ", cause);
        ctx.close();
    }

}