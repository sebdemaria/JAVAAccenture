$(function() {
    loadData()
});

function updateView(data) {
    let htmlList = data.map(function (game) {
        return  '<li>' + new Date(game.created).toLocaleString() + ' ' + game.gamePlayers.map(function(p) { return p.player.email}).join(',')  +'</li>';
    }).join('');
  document.getElementById("games-list").innerHTML = htmlList;
}

// load and display JSON sent by server for /players

function loadData() {
    $.get("/api/games")
        .done(function(data) {
          updateView(data);
        })
        .fail(function( jqXHR, textStatus ) {
          alert( "Failed: " + textStatus );
        });
}