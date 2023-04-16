// prevent resubmit warning
if (window.history && window.history.replaceState && typeof window.history.replaceState === 'function') {
  window.history.replaceState(null, null, window.location.href);
}

document.addEventListener('DOMContentLoaded', function(event) {

  // handle back click
  var backLink = document.querySelector('.govuk-back-link');
  if (backLink !== null) {
    backLink.addEventListener('click', function(e){
      e.preventDefault();
      e.stopPropagation();
      window.history.back();
    });
  }
});

// Find first ancestor of el with tagName
// or undefined if not found
function upTo(el, tagName) {
  tagName = tagName.toLowerCase();

  while (el && el.parentNode) {
    el = el.parentNode;
    if (el.tagName && el.tagName.toLowerCase() == tagName) {
      return el;
    }
  }

  // Many DOM methods return null if they don't
  // find the element they are searching for
  // It would be OK to omit the following and just
  // return undefined
  return null;
}

if (typeof accessibleAutocomplete != 'undefined' && document.querySelector('.autocomplete') != null) {
  // load autocomplete
  var selectEl = document.querySelector('.autocomplete');
  if (selectEl && selectEl.style.display !== "none") {
    accessibleAutocomplete.enhanceSelectElement({
      autoselect: true,
      id: selectEl.id, // Important that id is the same
      defaultValue: "",
      minLength: 0,
      selectElement: selectEl,
      showAllValues: true
    });
  }

  // =====================================================
  // Polyfill autocomplete once loaded
  // =====================================================
  var checkForLoad = setInterval(checkForAutocompleteLoad, 500);
  var originalSelect = document.querySelector('select.autocomplete');
  var parentForm = upTo(originalSelect, 'form');

  function polyfillAutocomplete(){
    var combo = parentForm.querySelector('[role="combobox"]');
    // =====================================================
    // Update autocomplete once loaded with fallback's aria attributes
    // Ensures hint and error are read out before usage instructions
    // =====================================================
    if(originalSelect && originalSelect.getAttribute('aria-describedby') > ""){
      if(parentForm){
        if(combo){
          combo.setAttribute('aria-describedby', originalSelect.getAttribute('aria-describedby') + ' ' + combo.getAttribute('aria-describedby'));
        }
      }
    }

    // =====================================================
    // Ensure when user replaces valid answer with a non-valid answer, then valid answer is not retained
    // =====================================================
    var holdSubmit = true;
    parentForm.addEventListener('submit', function(e){
      if(holdSubmit){
        e.preventDefault()
        if(originalSelect.querySelectorAll('[selected]').length > 0 || originalSelect.value > ""){

          var resetSelect = false;

          if(originalSelect.value){
            if(combo.value != originalSelect.querySelector('option[value="' + originalSelect.value +'"]').text){
              resetSelect = true;
            }
          }
          if(resetSelect){
            originalSelect.value = "";
            if(originalSelect.querySelectorAll('[selected]').length > 0){
              originalSelect.querySelectorAll('[selected]')[0].removeAttribute('selected');
            }
          }
        }

        holdSubmit = false;
        //parentForm.submit();
        HTMLFormElement.prototype.submit.call(parentForm); // because submit buttons have id of "submit" which masks the form's natural form.submit() function
      }
    })

  }
  function checkForAutocompleteLoad(){
    if(parentForm.querySelector('[role="combobox"]')){
      clearInterval(checkForLoad)
      polyfillAutocomplete();
    }
  }
}
