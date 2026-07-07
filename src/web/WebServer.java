package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.Student;
import service.RollCallService;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

// 程序主入口：提供基于本地 8080 端口的 Web 点名系统展示
public class WebServer {
    private static final RollCallService service = new RollCallService();

    public static void main(String[] args) throws Exception {
        // 创建 HTTP 服务器并绑定 8080 端口
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new web.WebServer.IndexHandler());
        server.createContext("/rollcall", new web.WebServer.RollCallHandler());
        server.setExecutor(null);

        System.out.println("=================================================");
        System.out.println(" 课堂随机点名系统已经成功启动！");
        System.out.println(" 请在浏览器中访问：http://localhost:8080/");
        System.out.println("=================================================");
        server.start();
    }

    // 主页处理器：负责生成 HTML 页面结构
    static class IndexHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 1. 从 service 获取当前状态
            Student luckyGuy = service.getLuckyGuy();
            int failCount = service.getContinuousFailCount();
            int N = service.getN();
            boolean allFailed = service.isAllFailedWarning();

            // 2. 动态拼接 HTML
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>")
                    .append("<meta http-equiv='Cache-Control' content='no-cache, no-store, must-revalidate'>")
                    .append("<title>学生课堂点名系统</title>")
                    .append("<style>")
                    .append("* { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Arial', sans-serif; }")
                    .append("body { background-color: #f4f6f9; color: #333; padding: 30px; display: flex; flex-direction: column; align-items: center; }")
                    .append(".container { width: 100%; max-width: 1000px; }")
                    .append("h2 { text-align: center; color: #2c3e50; margin-bottom: 25px; }")
                    .append(".dashboard { background: white; border-radius: 12px; padding: 25px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); text-align: center; margin-bottom: 30px; position: relative; }")
                    .append(".status-bar { font-size: 14px; color: #7f8c8d; margin-bottom: 15px; }")
                    .append(".danger-mode { color: #e74c3c; font-weight: bold; }")
                    .append(".all-failed-box { background-color: #fce4d6; border: 2px dashed #e74c3c; border-radius: 8px; padding: 20px; color: #c0392b; font-size: 20px; font-weight: bold; margin: 15px 0; }")
                    .append(".name-display { font-size: 56px; font-weight: bold; color: #3498db; margin: 15px 0; min-height: 75px; }")
                    .append(".btn-group { display: flex; justify-content: center; gap: 15px; margin-top: 15px; }")
                    .append(".btn { padding: 12px 28px; font-size: 16px; font-weight: bold; border: none; border-radius: 6px; cursor: pointer; text-decoration: none; display: inline-block; }")
                    .append(".btn-primary { background-color: #3498db; color: white; }")
                    .append(".btn-success { background-color: #2ecc71; color: white; }")
                    .append(".btn-danger { background-color: #e74c3c; color: white; }")
                    .append(".btn-warning { background-color: #95a5a6; color: white; position: absolute; top: 15px; right: 15px; padding: 6px 12px; font-size: 12px; }")
                    .append(".btn-disabled { background-color: #e0e0e0; color: #a0a0a0; cursor: not-allowed; pointer-events: none; opacity: 0.6; }")
                    .append(".table-card { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }")
                    .append(".table-title { font-size: 16px; font-weight: bold; color: #2c3e50; margin-bottom: 15px; padding-bottom: 10px; border-bottom: 2px solid #3498db; }")
                    .append("table { width: 100%; border-collapse: collapse; text-align: center; }")
                    .append("th { background-color: #f8f9fa; color: #34495e; padding: 12px; border-bottom: 2px solid #ddd; }")
                    .append("td { padding: 12px; border-bottom: 1px solid #eee; font-size: 14px; }")
                    .append(".highlight { background-color: #eaf2f8 !important; font-weight: bold; color: #2980b9; }")
                    .append("</style></head><body><div class='container'>")
                    .append("<h2>学生课堂随机点名系统</h2>")
                    .append("<div class='dashboard'>");

            html.append("<a href='/rollcall?action=reset' class='btn btn-warning' onclick='return confirm(\"确定要清空数据重置吗？\")'>重置数据</a>")
                    .append("<div class='status-bar'>");
            if (failCount >= N) {
                html.append("<span class='danger-mode'>警告：连续未答出已达 ").append(failCount).append(" 人！当前进入学霸低频优先模式</span>");
            } else {
                html.append("<span>状态正常：班级随机轮询中 (当前连续未答出: ").append(failCount).append("/").append(N).append(")</span>");
            }
            html.append("</div>");

            if (allFailed) {
                html.append("<div class='all-failed-box'>提示：全班学生对此题均无法作答，建议老师直接讲解该题。</div>");
            } else {
                html.append("<div class='name-display'>")
                        .append(luckyGuy != null ? luckyGuy.getName() : "等待抽取...")
                        .append("</div>");
            }

            html.append("<div class='btn-group'>");
            if (luckyGuy == null) {
                html.append("<a href='/rollcall?action=pick' class='btn btn-primary'>抽取下一位同学</a>");
                html.append("<a href='#' class='btn btn-disabled'>回答正确</a>");
                html.append("<a href='#' class='btn btn-disabled'>未能回答/错误</a>");
            } else {
                html.append("<a href='#' class='btn btn-disabled'>抽取下一位同学</a>");
                html.append("<a href='/rollcall?action=result&id=").append(luckyGuy.getId()).append("&correct=true' class='btn btn-success'>回答正确</a>");
                html.append("<a href='/rollcall?action=result&id=").append(luckyGuy.getId()).append("&correct=false' class='btn btn-danger'>未能回答/错误</a>");
            }

            html.append("</div></div>")
                    .append("<div class='table-card'><div class='table-title'>学生点名统计台账</div>")
                    .append("<table><thead><tr><th>学号</th><th>学生姓名</th><th>被点名次数</th><th>回答正确次数</th><th>实时答对率</th></tr></thead><tbody>");

            for (Student s : service.getStudentList()) {
                boolean isTarget = (luckyGuy != null && luckyGuy.getId().equals(s.getId()));
                html.append("<tr class='").append(isTarget ? "highlight" : "").append("'>")
                        .append("<td>").append(s.getId()).append("</td>")
                        .append("<td>").append(s.getName()).append("</td>")
                        .append("<td>").append(s.getCallCount()).append(" 次</td>")
                        .append("<td>").append(s.getRightCount()).append(" 次</td>")
                        .append("<td style='color: #3498db; font-weight: bold;'>").append(s.getRightRatio()).append("</td>")
                        .append("</tr>");
            }

            html.append("</tbody></table></div></div></body></html>");

            // 3. 将 HTML 字节响应给浏览器
            byte[] response = html.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    // 行为接收处理器：处理点击事件并进行页面跳转
    static class RollCallHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 1. 解析 URL 查询参数（如 ?action=pick&id=1001&correct=true）
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = new HashMap<>();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] kv = pair.split("=");
                    if (kv.length == 2) {
                        params.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                    }
                }
            }
            // 2. 根据 action 调用对应 service 方法
            String action = params.getOrDefault("action", "");
            if ("pick".equals(action)) {
                service.executeRollCall();
            } else if ("result".equals(action)) {
                service.handleResult(params.getOrDefault("id", ""), "true".equals(params.get("correct")));
            } else if ("reset".equals(action)) {
                service.resetToDefault();
            }

            // 3. 重定向回主页（302 状态码）
            exchange.getResponseHeaders().set("Location", "/");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        }
    }
}