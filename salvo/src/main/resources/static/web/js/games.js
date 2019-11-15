$(function() {
    loadData()
});
function updateView(data) {
    let htmlList = data.map(function (game) {
        return  '<li>' + new Date(game.creationDate).toLocaleString() + ' ' + game.gamePlayers.map(function(gamePlayer) { return gamePlayer.player.email}).join(',')  +'</li>';
    }).join('');
  document.getElementById("games-list").innerHTML = htmlList;
}
function loadData() {
    $.get("/api/games")
        .done(function(data) {
          updateView(data);
        })
        .fail(function( jqXHR, textStatus ) {
          alert( "Failed: " + textStatus );
        });
}