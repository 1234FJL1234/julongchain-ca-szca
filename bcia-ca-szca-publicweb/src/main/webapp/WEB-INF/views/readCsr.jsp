<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<%@include file="common/metaHead.jsp"%>
<link rel="stylesheet" type="text/css" href="assets/css/wang.css" />
</head>
<body>
	<!-- START nav -->
	<jsp:include page="common/topNav.jsp" flush="true">
		<jsp:param value="menu_csr" name="currMenu" />
	</jsp:include>

	<!-- END nav -->


	<section class="probootstrap-cover overflow-hidden relative" style="background-image: url('assets/images/bg_1.jpg');" data-stellar-background-ratio="0.5"
		id="section-home">
		<div class="overlay"></div>
		<div class="container">
			<div class="row align-items-center">
				<div class="col-md">
					<h2 class="heading mb-2 display-4 font-light probootstrap-animate">解析CSR</h2>
					<p class="lead mb-5 probootstrap-animate">输入证书请求信息</p>
					<%-- 
					<p class="lead mb-5 probootstrap-animate"></p>
					<a href="onepage.html" role="button" class="btn btn-primary p-3 mr-3 pl-5 pr-5 text-uppercase d-lg-inline d-md-inline d-sm-block d-block mb-3">See
						OnePage Verion</a>
					</p> --%>
				</div>
				 
				<div class="col-md-right">
				<!-- <div >
				<iframe name="downloadEndEntityCertFrame" frameborder=0 width=0 height=0></iframe>
				  </div> -->
					<form name="decodeCsrForm" id="decodeCsrForm" action="decodeCsr.html" method="post"  class="probootstrap-form">
					
					<div class="row mb-3">
								<div class="col-md">
									<div class="form-group">
										<span id="id_testResultMsg" style="color: red"></span>										 
									</div>
								</div>							 
							</div>
						<div class="form-group">
							<div class="row mb-3">
								<div class="col-md">
									<div class="form-group">
										<label for="id_label_single1">证书请求（CSR）</label> 
										<textarea rows="8" style="font-size: 15px" cols="80" name="csr" required="required" placeholder="-----BEGIN CERTIFICATE REQUEST-----
MIIBLjCB1QIBADBzMRIwEAYDVQQDDAnmtYvor5VDU1IxETAPBgNVBAoMCOa3seWc
s0NBMRUwEwYDVQQLDAzmioDmnK/kuK3lv4MxEjAQBgNVBAcMCea3seWcs+W4gjES
MBAGA1UECAwJ5bm/5Lic55yBMQswCQYDVQQGEwJDTjBZMBMGByqGSM49AgEGCCqB
HM9VAYItA0IABFDaJn5itGjRvqBBOqkqMoha53y3m5T5suAcppeZwHe0iWFzie3j
O6N8mL4w7DCP3YFf0nI6hmMJS/Dor11VpKCgADAKBggqgRzPVQGDdQNIADBFAiAA
0VEYSK3kpn8E+e9rLPJLEZWHyFwoazaFp/7fJuB4bAIhAMJP5gf94wosrSjs1+ZF
zEKnWNz8GfmfWZqAYF4yJEbZ
-----END CERTIFICATE REQUEST-----"></textarea>										 
									</div>
								</div>
								
							</div>
							
							<!-- END row -->
							<div class="row">

								<div class="col-md">
									<input type="submit" value="提交" class="btn btn-primary btn-block" />
								</div>
							</div>
						</div>
					</form>
				</div> 
			</div>
		</div>

	</section>
	<!-- END section -->
 

	<!-- START Footer -->
	<%@include file="common/footer.jsp" %>
	<!-- END Footer -->
</body>
<script>
function downloadEECert(){
	//downloadEndEntityCertForm.target="downloadEndEntityCertFrame";
	downloadEndEntityCertForm.action="genEndEntityCert.html";	
	downloadEndEntityCertForm.submit();
}
</script>
<script type="text/javascript">
$(document).ready(function(){
	$(".js-example-basic-single").select2({
        minimumResultsForSearch: -1
	});
});
</script>
</html>