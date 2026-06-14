// Minimal JS for toggling notifications and account menus
document.addEventListener('DOMContentLoaded', function(){
  const notifBtn = document.getElementById('btn-notif');
  const notifMenu = document.getElementById('notif-menu');
  const accountBtn = document.getElementById('btn-account');
  const accountMenu = document.getElementById('account-menu');

  function hideAll(){
    if(notifMenu) notifMenu.classList.remove('show');
    if(accountMenu) accountMenu.classList.remove('show');
  }

  if(notifBtn && notifMenu){
    notifBtn.addEventListener('click', (e)=>{
      e.stopPropagation();
      notifMenu.classList.toggle('show');
      if(accountMenu) accountMenu.classList.remove('show');
    });
  }
  if(accountBtn && accountMenu){
    accountBtn.addEventListener('click', (e)=>{
      e.stopPropagation();
      accountMenu.classList.toggle('show');
      if(notifMenu) notifMenu.classList.remove('show');
    });
  }

  document.addEventListener('click', hideAll);
});

