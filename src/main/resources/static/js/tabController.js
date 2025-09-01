
	console.log('tabController.js loaded');
  function switchTab(id) {
    $('#TabmenuBottom .tab-pane').each(function(){
      $(this).prop('hidden', this.id !== 'pane-' + id);
    });
    $('#TabmenuTop .tab').each(function(){
      $(this).toggleClass('active', $(this).data('tab-id') === id);
    });
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

    // 탭 내용 AJAX 로드 후 전환
    $.get(url, function(html){
      $('#TabmenuBottom').append(
        `<div class="tab-pane" id="pane-${tabId}">${html}</div>`
      );
      switchTab(tabId);
    }).fail(function(){
      // 실패 시 탭 제거
      $('#tab-' + tabId).remove();
      alert('페이지를 불러오지 못했습니다.');
    });
  }

  // 이벤트 위임: 탭 헤더 클릭 → 전환
  $(document).on('click', '#TabmenuTop .tab', function(e){
    // 닫기 버튼 클릭은 제외
    if ($(e.target).closest('.tab-close').length) return;
    switchTab($(this).data('tab-id'));
  });

  // 닫기 버튼
  $(document).on('click', '#TabmenuTop .tab .tab-close', function(e){
    e.stopPropagation();
    const id = $(this).closest('.tab').data('tab-id');
    closeTab(id);
  });

  // 헤더 메뉴 클릭 → 탭 열기
  $(document).on('click', 'a.open-tab', function(e){
    e.preventDefault();
    const $a = $(this);
    openTab($a.data('tab-id'), $a.data('title'), $a.data('url'));
  });

