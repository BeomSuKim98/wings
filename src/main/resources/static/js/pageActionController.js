// AEI notice toggle - jQuery version
// HTML 구조: <li class="noticeContentListItem"><strong>제목</strong><p>내용</p></li>

$(function () {
  var $items = $('.noticeContentListItem');

  $items.each(function (idx) {
    var $item   = $(this);
    var $header = $item.children('strong').first();
    var $panel  = $item.children('p').first();
    if (!$header.length || !$panel.length) return;

    // 접근성 속성
    var panelId = $panel.attr('id') || ('notice-panel-' + idx);
    $panel.attr('id', panelId);
    $header.attr({
      'role': 'button',
      'tabindex': 0,
      'aria-controls': panelId,
      'aria-expanded': 'false'
    });

    // 초기 상태: SCSS가 height:0로 숨김 처리하므로 display:block + height:0 보장
    $panel.css({ display: 'block', height: 0 });

    function openPanel() {
      // 자연 높이 계산
      var target = $panel[0].scrollHeight;

      // height 애니메이션
      $item.addClass('is-open');
      $panel
        .stop(true, true)
        .animate({ height: target }, 280, 'swing', function () {
          // 애니메이션 종료 후 auto로 고정 (컨텐츠 늘어날 때 자연스러움)
          $panel.css('height', 'auto');
        });

      $header.attr('aria-expanded', 'true');
    }

    function closePanel() {
      // auto -> 현재 px값으로 고정 후 0으로 애니메이션
      var current = $panel.outerHeight();
      $panel.css('height', current);

      $panel
        .stop(true, true)
        .animate({ height: 0 }, 280, 'swing', function () {
          $item.removeClass('is-open');
        });

      $header.attr('aria-expanded', 'false');
    }

    function togglePanel() {
      if ($item.hasClass('is-open')) {
        closePanel();
      } else {
        openPanel();
      }
    }

    // 클릭/키보드 조작
    $header.on('click', function (e) {
      e.preventDefault();
      togglePanel();
    });

    $header.on('keydown', function (e) {
      // Space(32) 또는 Enter(13)
      if (e.key === ' ' || e.key === 'Enter' || e.keyCode === 32 || e.keyCode === 13) {
        e.preventDefault();
        togglePanel();
      }
    });
  });
});
