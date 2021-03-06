<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<jsp:include page="/jsp/template/adminHeader.jsp"/>
<jsp:include page="/jsp/template/adminSidebar.jsp" />
<link rel="stylesheet" href="http://api.map.baidu.com/library/SearchInfoWindow/1.5/src/SearchInfoWindow_min.css" />
    <div class="col-xs-12 col-sm-12 col-md-10 col-lg-10 content">
      	<div class="page-header">
			<h1>修改打印店</h1>
		</div>
		<c:if test="${!empty errorInfo}">
		<script type="text/javascript">
            alert("请求数据有错！");
        </script>
        </c:if>
		<form class="form-horizontal" role="form" method="post" action="updatePrinterInfo.do" enctype="multipart/form-data">
			<div class="form-group">
                <label for="printer-name" class="col-md-3 control-label">打印店名</label>
                <input type="text" class="form-control hidden" name="printer-id" id="printer-id" value="${param.id}">
                <div class="col-md-4">
                <input type="text" class="form-control" id="printer-name" name="printer-name" value="${printShopName}" placeholder="输入中文、字母或数字，长度最大32" autocomplete="off">
                </div>
                <div class="col-md-5">
                	<div class="alert alert-danger hidden" role="alert" id="alert-printer-name">
                		<span class="glyphicon glyphicon-remove" aria-hidden="true"></span>&nbsp;&nbsp;
                		<span id="alert-printer-name-message"></span>
                	</div>
                	<div class="alert hidden" role="alert" id="alert-printer-name-ok"></div>
                </div>
            </div>
            <div class="form-group">
                <label for="printer-username" class="col-md-3 control-label">店主名</label>
                <div class="col-md-4">
                <input type="text" class="form-control" id="printer-username" name="printer-username" value="${printerName}" placeholder="输入中文、字母或数字，长度最大32" autocomplete="off">
                </div>
                <div class="col-md-5">
                	<div class="alert alert-danger hidden" role="alert" id="alert-printer-username">
                		<span class="glyphicon glyphicon-remove" aria-hidden="true"></span>&nbsp;&nbsp;
                		<span id="alert-printer-username-message"></span>
                	</div>
                	<div class="alert hidden" role="alert" id="alert-printer-username-ok"></div>
                </div>
            </div>
            <div class="form-group">
                <label for="email" class="col-md-3 control-label">邮箱</label>
                <div class="col-md-4">
                <input type="text" class="form-control" id="email" name="email" value="${email}" placeholder="输入邮箱地址" autocomplete="off">
                </div>
                <div class="col-md-5">
                	<div class="alert alert-danger hidden" role="alert" id="alert-email">
                		<span class="glyphicon glyphicon-remove" aria-hidden="true"></span>&nbsp;&nbsp;
                		<span id="alert-email-message"></span>
                	</div>
                	<div class="alert hidden" role="alert" id="alert-email-ok"></div>
                </div>
            </div>
            <div class="form-group">
				<label for="address-choice" class="col-md-3 control-label" id="address-title">选择地址</label>
				<div class="col-xs-12 col-sm-2 col-md-2">
					<select class="form-control" id="province" name="province"></select>
				</div>
				<div class="col-xs-12 col-sm-2 col-md-2">
					<select type="text" class="form-control" id="city" name="city"></select>
				</div>
				<div class="col-xs-12 col-sm-2 col-md-2">
					<select class="form-control hidden" id="area" name="area"></select>
				</div>
			</div>
			<div class="form-group">
				<div class="col-md-offset-3 col-xs-12 col-sm-7 col-md-7">
					<input type="text" class="hidden" id="city-area-id" name="city-area-id" autocomplete="off">
					<input type="text" class="hidden" id="cascade-address" name="cascade-address" autocomplete="off">
					<input type="text" class="form-control" id="street" name="street" value="${street}" autocomplete="off" placeholder="街道地址（不能包含特殊字符）">
				</div>
			</div>
			<div class="form-group">
				<div class="col-md-offset-3 col-xs-12 col-sm-3 col-md-3">
					<input type="text" class="form-control" id="address-telenumber" autocomplete="off"
						name="address-telenumber" value="${teleNumber}" placeholder="联系号码">
				</div>
				<div class="col-md-4">
	             	<div class="alert alert-danger hidden" role="alert" id="alert-address-telenumber">
	             		<span class="glyphicon glyphicon-remove" aria-hidden="true"></span>&nbsp;&nbsp;
	             		<span id="alert-address-telenumber-message"></span>
	             	</div>
	             	<div class="alert hidden" role="alert" id="alert-address-telenumber-ok"></div>
            	</div>
			</div>
			<div class="form-group">
			<label for="longitude" class="col-md-3 control-label">地图</label>
			<div class="col-md-7" id="printer-delivery-address-map">
				<span class="btn btn-sm btn-default" id="address-map-btn"> <span
					class="glyphicon glyphicon-globe"></span>&nbsp;&nbsp;打开地图拾取经纬度 </span>
				<div id="l-map" class="hidden"></div>
			</div>
			</div>
			<div class="form-group">
			<label for="longitude" class="col-md-3 control-label">经度</label>
                <div class="col-md-3">
                <input type="text" class="form-control" id="longitude" name="longitude" value="${longitude}" placeholder="输入经度" autocomplete="off">
                </div>
                <div class="col-md-5">
                	<div class="alert alert-danger hidden" role="alert" id="alert-longitude">
                		<span class="glyphicon glyphicon-remove" aria-hidden="true"></span>&nbsp;&nbsp;
                		<span id="alert-longitude-message"></span>
                	</div>
                	<div class="alert hidden" role="alert" id="alert-longitude-ok"></div>
                </div>
            </div>
            <div class="form-group">
                <label for="latitude" class="col-md-3 control-label">纬度</label>
                <div class="col-md-3">
                <input type="text" class="form-control" id="latitude" name="latitude" value="${latitude}" placeholder="输入纬度" autocomplete="off">
                </div>
                <div class="col-md-5">
                	<div class="alert alert-danger hidden" role="alert" id="alert-latitude">
                		<span class="glyphicon glyphicon-remove" aria-hidden="true"></span>&nbsp;&nbsp;
                		<span id="alert-latitude-message"></span>
                	</div>
                	<div class="alert hidden" role="alert" id="alert-latitude-ok"></div>
                </div>
            </div>
            <div class="form-group">
            	<label for="shop-image" class="col-md-3 control-label">上传照片</label>
            	<div class="col-md-2">
            		<div class="avatar">
            		<c:choose>
						<c:when test="${(!empty printShopImage)}">
						    <img src="${printShopImage}" width="150" height="150">
						</c:when>
						<c:otherwise>
							<img src="images/photo.png" width="150" height="150">
						</c:otherwise>
					</c:choose>
					</div>
            	</div>
	            <div class="col-md-6">
					<p class="print-tip">从电脑中选择你喜欢的照片:<br>你可以上传JPG、JPEG、GIF、PNG或BMP文件。</p>
					<div class="avatar-upload btn btn-sm btn-default">
						<span>选择照片</span>
						<input class="upload" id="shop-image" name="shop-image" type="file">
					</div>
					<p id="shop-image-name" class="print-tip"></p>
				</div>
            </div>
            <div class="form-group">
                <div class="col-md-offset-3 col-md-3 form-btn-group">
                <button type="submit" name="submit" class="btn btn-primary" id="printer-submit">
                <span class="glyphicon glyphicon-edit"></span>&nbsp;&nbsp;修改</button>
                </div>
            </div>
		</form>
    </div>
</div>
<script type="text/javascript"
	src="http://api.map.baidu.com/api?v=2.0&ak=GpDZDSdhW0GYDfDMgV2eYHLO"></script>
<script type="text/javascript"
	src="http://api.map.baidu.com/library/SearchInfoWindow/1.5/src/SearchInfoWindow_min.js"></script>
<script src="js/adminAddPrinter.js"></script>
<script src="js/map.js"></script>
<script>
	$(document).ready(function(){
		var id = $("#printer-id").attr("value");
		$.ajax({
			type : "get",
			url : "getPrinterInfoById.do",
			async : true,
			dataType: 'json',
			data : {id: id},
			cache : false,
			success : function(data) {
				var address = data.result;
				console.log(address);
				$("#printer-name").val(address[0].printShopName);
				$("#printer-username").val(address[0].userName);
				$("#email").val(address[0].users.email);
				$("#street").val(address[0].address);
				$("#longitude").val(address[0].longitude);
				$("#latitude").val(address[0].latitude);
				$("#address-telenumber").val(address[0].teleNumber);
				$(".avatar img").attr("src",address[0].printShopImage);
				//更新得到市区列表
				var areaList = address[1].area;
				var cityList = address[1].city;
				var cityAreaId = address[0].cityAreaId;
				if(areaList){
					//三重级联
					var cityId=-1,provinceId=-1;
					for(var i=0;i<areaList.length;i++){
						option = $("<option value ='"+areaList[i].id+"'>"+areaList[i].name+"</option>");
						if(areaList[i].id==cityAreaId)
							cityId = areaList[i].pid;
						$("#area").append(option);
					}
					$("#area").val(cityAreaId);
					$("#area").removeClass("hidden");
					for(var i=0;i<cityList.length;i++){
						option = $("<option value ='"+cityList[i].id+"'>"+cityList[i].name+"</option>");
						if(cityList[i].id==cityId)
							provinceId = cityList[i].pid;
						$("#city").append(option);
					}
					$("#city").val(cityId);
					$("#province").val(provinceId);
					var province = $("#province").find("option:selected").text();
					var city = $("#city").find("option:selected").text();
					var area = $("#area").find("option:selected").text();
					$("#cascade-address").val(province+"／"+city+"／"+area+"／");
					$("#city-area-id").val($("#area").val());
				}
				else {
					//两重级联
					var cityId=cityAreaId,provinceId=-1;
					for(var i=0;i<cityList.length;i++){
						option = $("<option value ='"+cityList[i].id+"'>"+cityList[i].name+"</option>");
						if(cityList[i].id==cityId)
							provinceId = cityList[i].pid;
						$("#city").append(option);
					}
					$("#city").val(cityId);
					$("#province").val(provinceId);
					var province = $("#province").find("option:selected").text();
					var city = $("#city").find("option:selected").text();
					$("#cascade-address").val(province+"／"+city+"／");
					$("#city-area-id").val($("#city").val());
				}
			}
		});
	});
</script>
<jsp:include page="/jsp/template/footer.jsp"/>