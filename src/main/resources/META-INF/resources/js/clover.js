$(function() {
  var elems = document.querySelectorAll('.modal');
  var instances = M.Modal.init(elems, {
    startingTop: '50%'
  });

  var $searchResultLinks = $('#search-result-links');

  var buildDiaryEntryCollection = function(diaryEntries) {
    var collections = '<div class="collection">';

    diaryEntries.forEach(function(diaryEntry) {
      var categories = diaryEntry.categories.map(function(c) { return '[' + c + ']'; }).join('');

      collections += '<a href="' + diaryEntry.url + '" class="collection-item" target="_blank">' + diaryEntry.date + ' ' + categories + ' ' + diaryEntry.title + '</a>'
    });

    collections += '</div>';

    return collections;
  };

  $('#entries-refresh').on('click', function(e) {
    $.ajax({
      url: '/api/diary/refresh',
      cache: false,
      success: function() {
        M.toast({html: 'start diary entries, refresh...', displayLength: 3000});
      }
    });

    return true;
  });

  $('#query-text').on('keyup', function(e) {
    var query = e.target.value;

    if (query) {
      $.ajax({
        url: '/api/diary/search?query=' + encodeURIComponent(query),
        cache: false,
        dataType: 'json',
        success: function(diaryEntries) {
          var count = diaryEntries.length;

          if (count > 0) {
            $('#hit-count').text('Hits: ' + count);

            $searchResultLinks.empty();

            var collections = buildDiaryEntryCollection(diaryEntries);

            $searchResultLinks.append(collections);
          }
        }
      });
    }
  });

  var limit = 50

  $.ajax({
    url: '/api/diary?limit=' + limit,
    cache: false,
    dataType: 'json',
    success: function(diaryEntries) {
      $.ajax({
        url: '/api/diary/count',
        cache: false,
        dataType: 'json',
        success: function(data) {
          var count = data.count;
          $('#hit-count').text('Total: ' + diaryEntries.length + ' / ' + count);
        }
      });

      $searchResultLinks.empty();

      var collections = buildDiaryEntryCollection(diaryEntries);

      $searchResultLinks.append(collections);
    }
  });
});
