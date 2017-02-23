package one.two.three;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SlingServlet(label = "My AEM Servlet", description = "My AEM Servlet description", paths = {
    "/bin/myaemservlet" }, methods = { "GET" }, extensions = { "html" })
public class ThreeServlet9 extends SlingSafeMethodsServlet {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(ThreeServlet9.class);

  @Override
  protected final void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
      throws ServletException, IOException {

    LOG.info("Servlet GET Method requested");

    // Write a standard text/html response
    response.setContentType("text/html;charset=UTF-8");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write("<html><body>Text to write to response</body></html>");
  }
}