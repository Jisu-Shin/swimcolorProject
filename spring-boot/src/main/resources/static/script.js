// 햄버거 메뉴 토글
const hamburger = document.getElementById('hamburger');
const sidebar = document.getElementById('sidebar');
const overlay = document.getElementById('overlay');
const closeBtn = document.getElementById('closeBtn');

hamburger.addEventListener('click', () => {
    sidebar.classList.add('active');
    overlay.classList.add('active');
    hamburger.classList.add('active');
});

closeBtn.addEventListener('click', () => {
    closeSidebar();
});

overlay.addEventListener('click', () => {
    closeSidebar();
});

function closeSidebar() {
    sidebar.classList.remove('active');
    overlay.classList.remove('active');
    hamburger.classList.remove('active');
}

// 로고 클릭 시 홈으로
document.querySelector('.logo').addEventListener('click', () => {
    window.location.href = 'index.html';
});

// products.html로 이동
function goToProducts() {
    window.location.href = 'products.html';
}

// 상세 페이지로 이동 (products.html 내에서)
function goToProductDetail(productId) {
    // products.html에서 상세 페이지를 보여주도록 설정
    localStorage.setItem('selectedProductId', productId);
    window.location.href = 'products.html#detail';
}

// 검색 기능
document.addEventListener('DOMContentLoaded', () => {
    const searchBtn = document.querySelector('.search-btn');
    const searchInput = document.querySelector('.search-bar input');

    searchBtn.addEventListener('click', () => {
        const keyword = searchInput.value;
        if (keyword. trim()) {
            console.log('검색어:', keyword);
            // 추후 검색 기능 구현
        }
    });

    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            searchBtn.click();
        }
    });
});