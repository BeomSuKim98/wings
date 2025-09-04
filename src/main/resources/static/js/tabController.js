/* tabController.js */
console.log('tabController.js loaded');

/* --------------------------------
 * 유틸: 메뉴 포커스 제거 (focus-within 해제)
 * -------------------------------- */
function clearMenuFocus() {
  // 현재 포커스가 헤더 메뉴 내부라면 blur
  const active = document.activeElement;
  if (active && active.closest && active.closest('.topMenu')) {
    active.blur();
    // SPA 라우팅/탭 전환 비동기 타이밍 대비
    setTimeout(() => active.blur(), 0);
  }
}

/* --------------------------------
 * CSS 보장 로더 (없으면 정의)
 * -------------------------------- */
async function ensureCss(href) {
  return new Promise((resolve, reject) => {
    if (!href) return resolve();

    // 이미 로드되어 있으면 완료
    const exists = [...document.querySelectorAll('link[rel="stylesheet"]')]
      .some(link => link.href.includes(href));
    if (exists) return resolve();

    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = href;
    link.onload = () => resolve();
    link.onerror = () => reject(new Error(`CSS load failed: ${href}`));
    document.head.appendChild(link);
  });
}

/* --------------------------------
 * 탭 전환/닫기/열기
 * -------------------------------- */
function switchTab(id) {
  $('#TabmenuBottom .tab-pane').each(function () {
    $(this).prop('hidden', this.id !== 'pane-' + id);
  });
  $('#TabmenuTop .tab').each(function () {
    $(this).toggleClass('active', $(this).data('tab-id') === id);
  });

  // 탭 전환 시에도 남아있는 메뉴 포커스 제거 (메뉴 닫힘 보장)
  clearMenuFocus();
}

function closeTab(id) {
  $('#tab-' + id).remove();
  $('#pane-' + id).remove();
  const $first = $('#TabmenuTop .tab').first();
  if ($first.length) switchTab($first.data('tab-id'));
}

function openTab(tabId, title, url) {
  // 이미 열려 있으면 전환만
  if ($('#tab-' + tabId).length) { switchTab(tabId); return; }

  // 탭 헤더 생성
  $('#TabmenuTop').append(
    `<div class="tab" id="tab-${tabId}" data-tab-id="${tabId}">
       <span>${title}</span>
       <button type="button" class="tab-close" title="닫기">×</button>
     </div>`
  );

  // 단일 요청: HTML 로드 → meta[name="page-css"] 검사 → CSS 보장 → pane 추가 → 전환
  $.get(url, async function (html) {
    const $html = $(html);

    // 메타에서 페이지 전용 CSS 경로 추출
    const css =
      $html.filter('meta[name="page-css"]').attr('content') ||
      $html.find('meta[name="page-css"]').attr('content');

    try { if (css) await ensureCss(css); } catch (e) { console.warn(e.message); }

    // pane 구성 (HTML 전체 삽입)
    $('#TabmenuBottom').append(`<div class="tab-pane" id="pane-${tabId}"></div>`)
                       .children().last().append($html);

    switchTab(tabId);
  }).fail(function () {
    // 실패 시 탭 제거
    $('#tab-' + tabId).remove();
    alert('페이지를 불러오지 못했습니다.');
  });
}

/* --------------------------------
 * 이벤트 위임
 * -------------------------------- */

// 탭 헤더 클릭 → 전환 (닫기 버튼 제외)
$(document).on('click', '#TabmenuTop .tab', function (e) {
  if ($(e.target).closest('.tab-close').length) return;
  switchTab($(this).data('tab-id'));
});

// 닫기 버튼
$(document).on('click', '#TabmenuTop .tab .tab-close', function (e) {
  e.stopPropagation();
  const id = $(this).closest('.tab').data('tab-id');
  closeTab(id);
});

// 헤더 메뉴 클릭 → 탭 열기 (data-* 기반)
$(document).on('click', 'a.open-tab', function (e) {
  e.preventDefault();
  const $a = $(this);

  openTab($a.data('tab-id'), $a.data('title'), $a.data('url'));

  // 클릭 직후 포커스 제거 → :focus-within 해제(2차 메뉴 닫힘)
  this.blur();
  setTimeout(() => this.blur(), 0);
});

// 헤더 메뉴의 모든 링크에 대해: 클릭 시 포커스 제거 (일반 링크 포함)
$(document).on('click', '.topMenu a', function () {
  this.blur();
  setTimeout(() => this.blur(), 0);
});

// 바깥 영역 클릭 시에도 혹시 남은 포커스가 있으면 정리
document.addEventListener('click', function (e) {
  const inMenu = e.target.closest && e.target.closest('.topMenu');
  if (!inMenu) clearMenuFocus();
});

// 키보드 접근성: Enter/Space로 클릭 트리거 후 blur
$(document).on('keydown', '.topMenu a', function (e) {
  if (e.key === 'Enter' || e.key === ' ') {
    // 기본 동작을 유지하되(Enter는 링크 이동), blur 예약
    setTimeout(() => this.blur(), 0);
  }
});
