/**
 * [1] 메인 초기화 객체
 */
var main = {
    init: function() {
        const _this = this;
        // 헤더 검색바 로직 초기화
        _this.initSearchBar();

        if ($('#swimsuit-list').length > 0) {
            console.log("수영복 리스트 페이지 진입 - 스크롤 이벤트 등록");
            swimsuitListModule.init();
        }
    },

    // 모바일 검색창 확장 로직
    initSearchBar: function() {
        const searchBtn = document.querySelector('.search-btn');
        const searchInput = document.querySelector('.search-bar input');
        const header = document.querySelector('.header');
        const searchBar = document.querySelector('.search-bar');

        if (!searchBtn) return;

        searchBtn.addEventListener('click', (e) => {
            if (window.innerWidth <= 767) {
                if (!header.classList.contains('search-active')) {
                    e.preventDefault();
                    header.classList.add('search-active');
                    searchInput.focus();
                }
            }
        });

        // 외부 클릭 시 축소
        document.addEventListener('click', (e) => {
            if (header.classList.contains('search-active') && !searchBar.contains(e.target)) {
                header.classList.remove('search-active');
            }
        });
    }
};

/**
 * [2] 페이지 이동 관련 함수 (Global Navigation)
 */
const nav = {
    goToSwimsuitDetail: function(id) {
        if (oper.isEmpty(id)) return alert('상품 정보를 찾을 수 없습니다.');
        location.href = `/swimsuits/${id}`;
    },
    goToSwimsuits: function() {
        location.href = '/swimsuits';
    },
    goBack: function() {
        history.back();
    }
};

/**
 * [3] 수영복 상세/추천 모듈
 */
const swimsuitModule = {
    // 추천 수모 목록 조회
    initRecommendCaps: function(swimsuitId, colors) {
        const data = { itemId: swimsuitId, colors: colors };

        oper.ajax("POST", data, `/api/swimsuits/${swimsuitId}/recommended-swimcaps`
            , (res) => {
                this.renderCaps(res);
            }
            , this.failRecommendCaps()
        );
    },

    // 수모 리스트 화면 렌더링
    renderCaps: function(caps) {
        const $container = $('#recommend-cap-list');
        if ($container.length === 0) return;

        $container.empty();

        if (!caps || caps.length === 0) {
            $container.append('<p class="no-data">추천할 수모가 없습니다.</p>');
            return;
        }

        caps.forEach(cap => {
            // 색상 칩 생성
            const colorChipsHtml = (cap.colors || []).map(color => `
                <span class="color-chip" style="background-color: ${color};" data-color="${color}"></span>
            `).join('');

            // 아이템 추가
            $container.append(`
                <div class="cap-item">
                    <img src="${cap.imageUrl}" alt="${cap.name}">
                    <div class="cap-info">
                        <p class="brand">${cap.brand}</p>
                        <p class="name">${cap.name}</p>
                        <p class="price">${Number(cap.price).toLocaleString()}원</p>
                    </div>
                    <div class="color-palette">
                        <span>대표 색상:</span>
                        <div class="chips-wrapper">${colorChipsHtml}</div>
                    </div>
                </div>
            `);
        });
    },

    failRecommendCaps: function() {
        const $container = $('#recommend-cap-list');
        if ($container.length === 0) return;

        $container.empty();
        $container.append('<p class="no-data">추천할 수모가 없습니다.</p>');
    }
};

/**
 * [3] 수영복 리스트 모듈
 */
const swimsuitListModule = {
    isFetching: false, // 상태 변수를 모듈 안으로 이동

    init: function() {
        const _this = this;

        $(window).on('scroll', function() {
            const scrollTop = $(window).scrollTop();
            const windowHeight = $(window).height();
            const documentHeight = $(document).height();

            if (scrollTop + windowHeight >= documentHeight - 100) {
                const $btn = $('#load-more-btn');
                const nextPage = $btn.data('next-page');

                // 호출 조건 체크
                if (!_this.isFetching && $btn.length > 0 && $btn.is(':visible')) {
                    _this.fetchNextPage($btn, nextPage);
                }
            }
        });
    },

    // 데이터를 가져오는 공통 함수
    fetchNextPage: function($btn, nextPage) {
        this.isFetching = true; // 로딩 시작

        const params = { page: nextPage };

        // AJAX 콜백에서 response뿐만 아니라 $btn도 함께 넘겨줘야 다음 처리가 가능합니다.
        oper.ajax("GET", params, "/api/swimsuits/next", (res) => {
            this.renderSwimsuits(res, $btn);
        });
    },

    renderSwimsuits: function(response, $btn) {
        const $grid = $('#products-preview-grid'); // HTML의 ID와 일치하는지 확인하세요!

        $.each(response.content, function(index, product) {
            const cardHtml = `
                <div class="product-preview-card" onclick="nav.goToSwimsuitDetail('${product.id}')">
                    <img src="${product.imageUrl}" alt="${product.name}">
                    <div class="product-preview-info">
                        <p>${product.brand}</p>
                        <p>${product.name}</p>
                        <p>${product.price.toLocaleString()}원</p>
                    </div>
                </div>`;
            $grid.append(cardHtml);
        });

        // 1. 현재 페이지 번호를 가져와서 1 증가시킵니다.
        const currentPage = $btn.data('next-page');
        $btn.data('next-page', currentPage + 1);

        // 2. 마지막 페이지면 버튼 숨기기
        if (response.last) {
            $btn.hide();
        }

        // 3. 로딩 상태 해제
        this.isFetching = false;
    }
}

/**
 * [4] 유틸리티 및 공통 함수
 */
var oper = {
    isEmpty: (v) => v === "" || v === null || v === undefined,

    ajax: function(type, data, url, successCallback, failCallback) {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

        $.ajax({
            type: type,
            url: url,
            contentType: 'application/json; charset=utf-8',
            data: type.toUpperCase() === 'GET' ? data : JSON.stringify(data),
            beforeSend: function(xhr) {
                if (csrfToken && csrfHeader) xhr.setRequestHeader(csrfHeader, csrfToken);
            }
        })
        .done(res => successCallback && successCallback(res))
        .fail(xhr => {
        console.error('요청 실패:', xhr);

        // 추가: 실패 콜백이 넘어왔다면 실행합니다.
        if (failCallback) {
            failCallback(xhr);
        } else {
            // 실패 콜백이 없을 때만 기본 alert를 띄웁니다.
            alert("에러 발생: " + (xhr.responseJSON?.message || "통신 오류"));
        }
    });
    },

    // 날짜 관련 유틸리티 (포맷 최적화)
    getFormattedDate: function(daysAgo = 0) {
        let date = new Date();
        date.setDate(date.getDate() - daysAgo);

        const f = (n) => String(n).padStart(2, "0");
        return `${date.getFullYear()}${f(date.getMonth()+1)}${f(date.getDate())}${f(date.getHours())}${f(date.getMinutes())}`;
    }
};

// 초기화 실행
$(document).ready(() => main.init());