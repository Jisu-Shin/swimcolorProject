/**
 * [1] 메인 초기화 객체
 */
var main = {
    init: function() {
        const _this = this;
        // 헤더 검색바 로직 초기화
        _this.initSearchBar();

        // [추가] 사이드바 초기화 호출
        sidebarModule.init();

        if ($('#swimsuit-list').length > 0) {
            console.log("수영복 리스트 페이지 진입 - 스크롤 이벤트 등록");
            swimsuitListModule.init();
        } else {
            console.log(location.pathname);
            if (!location.pathname.includes('/swimsuits/SS')) {
                // 다른 페이지로 이동하여서 수영복 캐시 제거
                swimsuitListModule.clearCache();
            }
        }

        // 검색모듈 초기화
        searchModule.init();
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
    goBack: function() {
        history.back();
    },
    goSearch: function() {
        const keyword = $('#search-kewords').val();

        if (!keyword || keyword.trim() === "") {
            alert("검색어를 입력해주세요.");
            return;
        }

        console.log(keyword);
        location.href = `/search?keywords=${encodeURIComponent(keyword)}`;
    }
};

/**
 * [3] 수영복 상세/추천 모듈
 */
const swimsuitModule = {
    // 추천 수모 목록 조회
    initRecommendCaps: function(swimsuitId, colors) {
        const $container = $('#recommend-cap-list');
        // [핵심] 1. 통신 시작 전 로딩 메시지 출력
        $container.empty().append('<p class="loading-text">추천 수모를 가져오는 중입니다...</p>');

        const data = { itemId: swimsuitId, colors: colors };
        oper.ajax("POST", data, `/api/swimsuits/${swimsuitId}/recommended-swimcaps`
            , (res) => {
                this.renderCaps(res);
            }
            , () => this.failRecommendCaps()
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
                    <a href="${cap.productUrl}" target="_blank" rel="noopener noreferrer">
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
                    </a>
                </div>
            `);
        });
    },

    failRecommendCaps: function() {
        const $container = $('#recommend-cap-list');
        if ($container.length === 0) return;

        $container.empty();
        $container.append('<p class="no-data">데이터를 불러오는 중 오류가 발생했습니다.</p>');
    }
};

/**
 * [3] 수영복 리스트 모듈
 */
const swimsuitListModule = {
    isFetching: false, // 상태 변수를 모듈 안으로 이동
    storageKey: 'swimsuit_cache', // 세션 스토리지 키

    init: function() {
        const _this = this;
        const cache = sessionStorage.getItem(_this.storageKey);

        if (cache) {
            console.log("캐시가 있어요 - 복구 시작");
            const data = JSON.parse(cache);
            // 복구할 때는 기존 그리드를 비우고 캐시 데이터로 다시 그려야 합니다.
            $('#products-preview-grid').empty();
            _this.restoreList(data);

        } else {
            console.log("캐시가 없어요 - 초기 데이터 저장");
            _this.initFirstPageCache();
        }

        // 2. 스크롤 이벤트 등록
        $(window).on('scroll', function() {
            const scrollTop = $(window).scrollTop();
            const windowHeight = $(window).height();
            const documentHeight = $(document).height();

            if (scrollTop + windowHeight >= documentHeight - 100) {
                const $btn = $('#load-more-btn');
                const nextPage = $btn.data('next-page');

                // 호출 조건 체크
                if (!_this.isFetching && $btn.length > 0 && $btn.is(':visible')) {
                    console.log("다음 페이지 호출:", nextPage);
                    _this.fetchNextPage($btn, nextPage);
                }
            }
        });
    },

    initFirstPageCache: function() {
        const _this = this;
        const $firstPageItems = $('.product-preview-card');
        const initialContent = [];

        $firstPageItems.each(function() {
            const $card = $(this);

            // 중요: 서버에서 주는 JSON 필드명과 완벽히 일치시켜야 함
            initialContent.push({
                id: $card.data('id'),
                imageUrl: $card.find('img').attr('src'),
                brand: $card.find('.product-preview-info p:eq(0)').text(),
                name: $card.find('.product-preview-info p:eq(1)').text(),
                // '15,000원' -> 15000 (숫자만 추출)
                price: parseInt($card.find('.product-preview-info p:eq(2)').text().replace(/[^0-9]/g, ''))
            });
        });

        // 서버 응답(Page 객체)과 유사한 구조로 생성
        const initialData = {
            content: initialContent,
            nextPage: 2, // 1페이지는 이미 봤으니 다음은 2페이지
            last: $('#load-more-btn').is(':visible') === false
        };

        sessionStorage.setItem(_this.storageKey, JSON.stringify(initialData));
    },

    // 데이터를 가져오는 공통 함수
    fetchNextPage: function($btn, nextPage) {
        const _this = this;
        _this.isFetching = true; // 로딩 시작

        const params = { page: nextPage };

        const selected = [];
        $('input[name="brandFilter"]:checked').each(function() {
            selected.push($(this).val());
        });
        const brandString = selected.join(',');

        // 브랜드가 있는 경우 요청파라미터 세팅
        if (!oper.isEmpty(brandString)) {
            params.brands = brandString;
        }

        // AJAX 콜백에서 response뿐만 아니라 $btn도 함께 넘겨줘야 다음 처리가 가능합니다.
        oper.ajax("GET", params, "/api/swimsuits", (res) => {
            // 새 데이터를 가져오면 세션 스토리지에 누적 저장
            _this.saveToCache(res);
            _this.renderSwimsuits(res);
        }, (err) => {
             // 에러 시에도 반드시 플래그를 풀어줘야 다음 스크롤이 작동합니다.
             _this.isFetching = false;
        });
    },

    // 데이터를 세션에 누적하여 저장하는 함수
    saveToCache: function(newResponse) {
        console.log("캐시저장");
        const cache = sessionStorage.getItem(this.storageKey);
        let data = cache ? JSON.parse(cache) : { content: [], nextPage: 1, scrollPos: 0 };

        // 데이터 합치기
        data.content = data.content.concat(newResponse.content);
        data.nextPage = newResponse.number + 1; // 서버 응답 기준으로 갱신

        data.last = newResponse.last;

        sessionStorage.setItem(this.storageKey, JSON.stringify(data));
    },

    // 수영복 목록을 그리는 함수
    renderSwimsuits: function(response) {
        const $grid = $('#products-preview-grid'); // HTML의 ID와 일치하는지 확인하세요!

        let html = "";
        $.each(response.content, function(index, product) {
            html += `
                <div class="product-preview-card" data-id="${product.id}" onclick="swimsuitListModule.handleProductClick('${product.id}')">
                    <img src="${product.imageUrl}" alt="${product.name}">
                    <div class="product-preview-info">
                        <p>${product.brand}</p>
                        <p>${product.name}</p>
                        <p>${product.price.toLocaleString()}원</p>
                    </div>
                </div>`;
        });
        $grid.append(html);

        const $btn = $('#load-more-btn');

        // 💡 중요: 버튼의 다음 페이지 번호는 response 데이터 기반으로 갱신하는 게 가장 안전합니다.
        // response.number는 현재 페이지 번호이므로 +1을 해서 저장합니다.
        if (response.number !== undefined) {
            $btn.data('next-page', response.number + 1);
        }

        if (response.last) {
            console.log("마지막 페이지면 버튼 숨기기");
            $btn.hide();
        } else {
            $btn.show();
        }

        // 3. 로딩 상태 해제
        this.isFetching = false;
    },

    // 뒤로가기 시 화면을 복구하는 함수
    restoreList: function(data) {
        console.log("기존 데이터를 복구합니다...");
        const _this = this;

        // 1. 저장된 모든 데이터 렌더링
        _this.renderSwimsuits({ content: data.content, last: data.last });

        // 2. 다음 페이지 번호 갱신
        const $btn = $('#load-more-btn');
        $btn.data('next-page', data.nextPage);

        // 3. 스크롤 위치 복구 (데이터가 다 그려진 후 약간의 지연 필요)
        const savedScroll = sessionStorage.getItem(this.storageKey + '_pos');
        if (savedScroll) {
            setTimeout(() => window.scrollTo(0, parseInt(savedScroll)), 100);
        }

        // 브랜드 필터 상태 확인을 아주 약간 지연 실행 (브라우저 복구 시간 확보)
        setTimeout(() => {
            this.updateFilterState();
        }, 50); // 50ms (0.05초) 정도면 충분합니다.
    },

    // 상품 클릭 시 현재 스크롤 위치 저장 후 이동
    handleProductClick: function(id) {
        sessionStorage.setItem(this.storageKey + '_pos', $(window).scrollTop());
        nav.goToSwimsuitDetail(id);
    },

    // 세션 스토리지에 저장된 수영복 리스트 캐시와 스크롤 위치 초기화
    clearCache: function() {
        console.log("수영복 리스트 캐시를 초기화합니다.");
        sessionStorage.removeItem('swimsuit_cache');
        sessionStorage.removeItem('swimsuit_cache_pos');

        // 만약 페이징 처리를 하고 있다면 페이지 번호도 여기서 초기화하면 좋습니다.
        // this.currentPage = 0;
    },

    openBrandFilter: () => $('#brandFilterOverlay').fadeIn(250),
    closeBrandFilter: (e) => $('#brandFilterOverlay').fadeOut(200),

    updateFilterState: function() {
        const count = $('input[name="brandFilter"]:checked').length;
        console.log("브랜드 필터 상태확인", count);
        $('#selected-count').text(count);
    },

    resetFilters: function() {
        $('input[name="brandFilter"]').prop('checked', false);
        this.updateFilterState();
    },

    applyFilters: function() {
        const _this = this;
        const selected = [];
        $('input[name="brandFilter"]:checked').each(function() {
            selected.push($(this).val());
        });

        const brandString = selected.join(',');
        console.log(brandString);

        if(oper.isEmpty(brandString)) {
             _this.clearCache();
             location.href = `/swimsuits`;
             return;
        }

        const data = {
            'brands' : brandString
        };

        oper.ajax("GET", data, `/api/swimsuits`, (res) => {
             // 기존 캐시 삭제
             _this.clearCache();

             // 기존 컨테이너 지우기
             $('#products-preview-grid').empty();

             // 새 데이터를 가져오면 세션 스토리지에 누적 저장
             _this.saveToCache(res);
             _this.renderSwimsuits(res);
         }, (err) => {
              console.error("필터 적용 중 에러 발생:", err);
         });

        // 여기에 실제 필터링 로직 추가 (AJAX 등)
        this.closeBrandFilter();
    }
}

/** 검색페이지 모듈 **/
const searchModule = {
    init: function() {
        const _this = this;

        // 엔터키 이벤트 바인딩
        $('#search-page-input').on('keydown', function(e) {
            if (e.key === 'Enter' || e.keyCode === 13) {
                _this.doSearch();
            }
        });
    },

    doSearch: function() {
        const keyword = $('#search-page-input').val().trim();

        if (!keyword) {
            alert("검색어를 입력해주세요.");
            return;
        }

        // 1. 단순 페이지 이동 방식 (권장)
        // 검색 결과 페이지는 SEO와 URL 공유를 위해 쿼리 파라미터 방식을 주로 씁니다.
        location.href = `/search?keywords=${encodeURIComponent(keyword)}`;
    }
}

/**
 * [추가] 사이드바 모듈
 */
const sidebarModule = {
    init: function() {
        const hamburger = document.getElementById('hamburger');
        const sidebar = document.getElementById('sidebar');
        const overlay = document.getElementById('sidebarOverlay');
        if (!hamburger || !sidebar) return; // 요소가 없으면 실행 안함

        const closeBtn = sidebar.querySelector('.close-btn');
        const focusableSelector = 'a, button, input, [tabindex]:not([tabindex="-1"])';

        // 내부 함수: 열기
        const openSidebar = () => {
            sidebar.classList.add('open');
            overlay.classList.add('active');
            sidebar.setAttribute('aria-hidden', 'false');
            hamburger.setAttribute('aria-expanded', 'true');
            overlay.setAttribute('aria-hidden', 'false');

            const first = sidebar.querySelector(focusableSelector);
            if (first) first.focus();
            document.addEventListener('keydown', onKeyDown);
        };

        // 내부 함수: 닫기
        const closeSidebar = () => {
            sidebar.classList.remove('open');
            overlay.classList.remove('active');
            sidebar.setAttribute('aria-hidden', 'true');
            hamburger.setAttribute('aria-expanded', 'false');
            overlay.setAttribute('aria-hidden', 'true');
            hamburger.focus();
            document.removeEventListener('keydown', onKeyDown);
        };

        const onKeyDown = (e) => {
            if (e.key === 'Escape') closeSidebar();
        };

        // 이벤트 리스너 등록
        hamburger.addEventListener('click', () => {
            const isOpen = sidebar.classList.contains('open');
            isOpen ? closeSidebar() : openSidebar();
        });

        if (closeBtn) closeBtn.addEventListener('click', closeSidebar);
        if (overlay) overlay.addEventListener('click', closeSidebar);

        sidebar.querySelectorAll('a').forEach(a => {
            a.addEventListener('click', closeSidebar);
        });
    }
};

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