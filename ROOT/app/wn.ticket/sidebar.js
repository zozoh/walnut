([{
    title: "i18n:ticket.menu",
    items: [{
        ph: '~',
        icon: '<i class="zmdi zmdi-home"></i>',
        text: 'i18n:home'
    }, {
        ph: '~/.ticket',
        icon: '<i class="fa fa-ticket"></i>',
        text: 'i18n:ticket.dataconf'
    }, {
        ph: '~/.ticket/user',
        icon: '<i class="fa fa-user-circle"></i>',
        text: 'i18n:ticket.people',
        editor: 'edit_ticket_people'
    }, {
        ph: '~/.ticket/record',
        icon: '<i class="fa fa-bell-o"></i>',
        text: 'i18n:ticket.record',
        editor: 'edit_ticket_search'
    }]
}])