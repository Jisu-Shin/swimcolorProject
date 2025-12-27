var main = {
    init : function() {

        var _this = this;

    }
}

// 수영복 상세 페이지로 이동하는 함수
function goToSwimsuitDetail(id) {
    console.log(id);
    if (oper.isEmpty(id)) {
        alert('상품 정보를 찾을 수 없습니다.');
        return;
    }
    window.location.href = '/swimsuits/' + id;
}

// 수영복 목록 페이지로 이동하는 함수
function goToProducts() {
    window.location.href = '/swimsuits';
}

// 이전 페이지로 돌아가는 함수
function goBack() {
    window.history.back();
}

var callback = {

}

var oper = {
    isEmpty : function(value) {
        if(value == "" || value == null || value == undefined) {
            return true;
        } else {
            return false;
        }
    },

    ajax : function(type, data, url, callback) {
        // GET이면 쿼리스트링으로 붙이고 data는 제거
        if (type === "GET" && data && Object.keys(data).length > 0) {
            const queryString = $.param(data); // itemId=123&bookingStatus=BOOK
            url += (url.includes("?") ? "&" : "?") + queryString;
            data = null;
        }

        $.ajax({
            'type': type,
            'url':url,
//            'dataType':'json',
            'contentType':'application/json; charset=utf-8',
            'data': type === "GET" ? null : JSON.stringify(data)
        })
        .done(function(response){
            callback(response);
        })
        .fail(function(xhr, status, error) {
            // 요청이 실패했을 때 실행되는 코드
            console.error('요청 실패:', xhr);
            let errMsg = xhr.responseJSON?.message || xhr.responseText || error || '알 수 없는 오류';

            // Validation 에러인 경우 필드별 에러도 표시
            if (xhr.responseJSON?.errors) {
                let fieldErrors = xhr.responseJSON.errors;
                let errorDetails = '\n';
                for (let field in fieldErrors) {
                    errorDetails += `- ${fieldErrors[field]}\n`;
                }
                errMsg += errorDetails;
            }

            alert(errMsg);
        })
//        .always(function(){
//            console.log("ajax always 로그");
//        });
    },

    getTodayDt : function() {
        let today = new Date();
        let year = String(today.getFullYear());
        let month = String(today.getMonth()+1).padStart(2,"0");
        let date = String(today.getDate()).padStart(2,"0");
        let hours = String(today.getHours()).padStart(2,"0");
        let minutes = String(today.getMinutes()).padStart(2,"0");
        return year+month+date+hours+minutes;
    },

    getSevenDaysAgo : function() {
        let today = new Date();

        let sevenDaysAgo = new Date();
        sevenDaysAgo.setDate(today.getDate() - 7);

        let year = String(sevenDaysAgo.getFullYear());
        let month = String(sevenDaysAgo.getMonth()+1).padStart(2,"0");
        let date = String(sevenDaysAgo.getDate()).padStart(2,"0");
        let hours = String(sevenDaysAgo.getHours()).padStart(2,"0");
        let minutes = String(sevenDaysAgo.getMinutes()).padStart(2,"0");

        return year+month+date+hours+minutes;
    }
}

main.init();