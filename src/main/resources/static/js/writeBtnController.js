
// jQuery 필요: <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
$(function () {
  // ------ 유틸 ------
  function focusAndGet($ta) {
    const ta = $ta[0];
    // 포커스가 없으면 selectionStart 동작이 어색할 수 있으니 보정
    if (document.activeElement !== ta) $ta.focus();
    return ta;
  }

  function wrapSelection($ta, before, after) {
    const ta = focusAndGet($ta);
    const start = ta.selectionStart;
    const end   = ta.selectionEnd;
    const val   = $ta.val();
    const sel   = val.substring(start, end);
    const next  = val.substring(0, start) + before + sel + after + val.substring(end);

    $ta.val(next);
    const pos = start + before.length + sel.length + after.length;
    ta.setSelectionRange(pos, pos);
    $ta.trigger('input');
  }

  function insertAtCursor($ta, html) {
    const ta = focusAndGet($ta);
    const start= ta.selectionStart;
    const val  = $ta.val();
    const next = val.substring(0, start) + html + val.substring(start);

    $ta.val(next);
    const pos = start + html.length;
    ta.setSelectionRange(pos, pos);
    $ta.trigger('input');
  }

  function wrapBlock($ta, tagName) {
    const ta = focusAndGet($ta);
    const start = ta.selectionStart;
    const val   = $ta.val();

    const lineStart = val.lastIndexOf('\n', start - 1) + 1;
    const lineEnd   = val.indexOf('\n', start);
    const endIdx    = lineEnd === -1 ? val.length : lineEnd;
    const line      = val.substring(lineStart, endIdx).trim();

    const wrapped = `<${tagName}>${line}</${tagName}>`;
    const next    = val.substring(0, lineStart) + wrapped + val.substring(endIdx);

    $ta.val(next);
    const pos = lineStart + wrapped.length;
    ta.setSelectionRange(pos, pos);
    $ta.trigger('input');
  }

  function insertList($ta, type) {
    const ta   = focusAndGet($ta);
    const start= ta.selectionStart;
    const end  = ta.selectionEnd;
    const val  = $ta.val();

    const sel   = val.substring(start, end) || '항목1\n항목2';
    const lines = sel.split(/\r?\n/).filter(s => s.length > 0);
    const items = lines.map(s => `<li>${s}</li>`).join('');
    const html  = `<${type}>${items}</${type}>`;

    const next  = val.substring(0, start) + html + val.substring(end);
    $ta.val(next);

    const pos = start + html.length;
    ta.setSelectionRange(pos, pos);
    $ta.trigger('input');
  }

  // ------ 에디터 컨테이너 단위로 위임 ------
  // 한 페이지에 여러 .js-editor 블록이 있어도 각각 동작하도록 스코프를 좁혀 처리
  $(document).on('click', '.js-editor .js-toolbar button', function () {
    const $btn    = $(this);
    const $editor = $btn.closest('.js-editor');
    const $ta     = $editor.find('.js-editor-textarea');

    const cmd = $btn.data('cmd');
    if (cmd === 'wrap') {
      wrapSelection($ta, $btn.data('before'), $btn.data('after'));
    } else if (cmd === 'insert') {
      insertAtCursor($ta, $btn.data('html'));
    } else if (cmd === 'block') {
      wrapBlock($ta, $btn.data('tag'));
    } else if (cmd === 'list') {
      insertList($ta, $btn.data('type'));
    }
  });

  // 링크 삽입(위임, id 사용)
  $(document).on('click', '.js-editor .js-toolbar #btn-insert-link', function () {
    const $editor = $(this).closest('.js-editor');
    const $ta     = $editor.find('.js-editor-textarea');
    const url = window.prompt('링크 URL을 입력하세요 (http/https/mailto):', 'https://');
    if (!url) return;
    wrapSelection($ta, `<a href="${url}" rel="nofollow noopener noreferrer" title="">`, `</a>`);
  });

  // 글자색/배경색/폰트크기(위임, id 사용)
  $(document).on('click', '.js-editor .js-toolbar #btn-text-color', function () {
    const $ta = $(this).closest('.js-editor').find('.js-editor-textarea');
    const color = window.prompt('글자색 (예: red, #ff0000, rgb(255,0,0))');
    if (!color) return;
    wrapSelection($ta, `<span style="color:${color}">`, `</span>`);
  });

  $(document).on('click', '.js-editor .js-toolbar #btn-bg-color', function () {
    const $ta = $(this).closest('.js-editor').find('.js-editor-textarea');
    const color = window.prompt('배경색 (예: yellow, #ffff00)');
    if (!color) return;
    wrapSelection($ta, `<span style="background-color:${color}">`, `</span>`);
  });

  $(document).on('click', '.js-editor .js-toolbar #btn-font-size', function () {
    const $ta = $(this).closest('.js-editor').find('.js-editor-textarea');
    const size = window.prompt('폰트 크기 (예: 14px, 1.2em, 120%)');
    if (!size) return;
    wrapSelection($ta, `<span style="font-size:${size}">`, `</span>`);
  });

  // 이미지 버튼 → 업로드 input 트리거(위임, id 사용)
  $(document).on('click', '.js-editor .js-toolbar #btn-insert-image', function () {
    $(this).closest('form').find('.js-image-upload').trigger('click');
  });


  // 업로드 후 본문에 <img> 자동 삽입 (간이 미리보기: object URL)
  // ※ 실제 서버 업로드 후 반환 URL을 쓰려면 이 부분을 AJAX 업로드/응답 URL 삽입으로 바꾸세요.
  $(document).on('change', '.js-image-upload', function () {
    const file = this.files && this.files[0];
    if (!file) return;

    const $form   = $(this).closest('form');
    const $editor = $form.find('.js-editor');
    const $ta     = $editor.find('.js-editor-textarea');

    // 간단 미리보기용: object URL 사용
    const url = URL.createObjectURL(file);
    insertAtCursor($ta, `<img src="${url}" alt="">`);
    // 페이지 이탈 때 revoke해도 되고, 서버 업로드 후 교체해도 됩니다.
    // window.addEventListener('beforeunload', () => URL.revokeObjectURL(url), { once: true });
  });
});




// 미리보기용 코드
(function ($) {
  // ---------- 미리보기 ----------
  function refreshPreview() {
    var $ta = $('#contentHtml');
    var $pv = $('#preview');
    if ($ta.length && $pv.length) {
      $pv.html($ta.val());
    }
  }

  // ---------- 익명 표시 영역 토글 ----------
  function toggleAnonRow() {
    var $anonRow = $('#anonRow');
    if (!$anonRow.length) return;
    var $anonCk = $anonRow.prev('.form-row').find('input[type="checkbox"]');
    if (!$anonCk.length) return;
    $anonRow.toggle($anonCk.is(':checked'));
  }

  // ---------- 이미지 업로드 후 본문에 <img> 삽입 ----------
  function uploadImage(file) {
    if (!file) return;
    var formData = new FormData();
    formData.append('file', file);
    $.ajax({
      url: '/api/uploads/image',
      type: 'POST',
      data: formData,
      processData: false,
      contentType: false,
      success: function (res) {
        var $ta = $('#contentHtml');
        if (!$ta.length) return;
        var cur = $ta.val();
        var tag = '<img src="' + res.url + '" alt="image">';
        $ta.val(cur + (cur.trim() ? '\n' : '') + tag + '\n').trigger('input');
        $('#imageUpload').val('');
      },
      error: function (xhr) {
        alert('이미지 업로드 실패: ' + (xhr.responseText || xhr.status));
      }
    });
  }

  // ---------- 이벤트: 위임으로 늦게 붙는 DOM 대응 ----------
  $(document)
    .off('input.preview', '#contentHtml')
    .on('input.preview', '#contentHtml', refreshPreview)
    .off('change.preview', 'form input[type="checkbox"]')
    .on('change.preview', 'form input[type="checkbox"]', toggleAnonRow)
    .off('change.preview', '#imageUpload')
    .on('change.preview', '#imageUpload', function () {
      var file = this.files && this.files[0];
      uploadImage(file);
    });

  // ---------- 최초 1회 렌더: 등장 감지 후 즉시 해제 ----------
  function bootOnceWhenReady() {
    if ($('#contentHtml').length && $('#preview').length) {
      refreshPreview();
      toggleAnonRow();
      return;
    }
    var mo = new MutationObserver(function (muts, obs) {
      if ($('#contentHtml').length && $('#preview').length) {
        obs.disconnect(); // 무한루프 방지
        refreshPreview();
        toggleAnonRow();
      }
    });
    mo.observe(document.documentElement, { childList: true, subtree: true });
    // 10초 타임아웃 (혹시 모를 누수 방지)
    setTimeout(function () { try { mo.disconnect(); } catch (e) {} }, 10000);
  }

  // DOM 준비 후 1회 부팅
  $(function () {
    bootOnceWhenReady();
  });

  // ---------- SPA/탭 교체 시 수동 호출 ----------
  window.AEI_previewBoot = function () {
    bootOnceWhenReady();
  };
})(jQuery);
